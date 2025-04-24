package com.kuang.cachestudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuang.cachestudy.mapper.PostsMapper;
import com.kuang.cachestudy.pojo.Posts;
import com.kuang.cachestudy.service.PostsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author ch
 * @description 针对表【posts】的数据库操作Service实现
 * @createDate 2025-04-07 18:59:53
 * 此为弱一致性缓存，对于并发的处理更为强大
 * 若需要强一致性缓存则改为点赞后双写（Redis和MySQL）
 */
@Service
public class PostsServiceImpl extends ServiceImpl<PostsMapper, Posts> implements PostsService {
    @Resource
    private PostsMapper postsMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 当缓存增量达到此值时，写一次数据库
    private static final int LIKE_COUNT_DELTA = 50;
    // 每过此值的时间，更新到数据库（单位：毫秒）
    private static final int LIKE_COUNT_UPDATE_INTERVAL = 10 * 60 * 1000;
    // 每过此值的时间，缓存中点赞的增量过期（需大于上述的时间，单位：秒）
    private static final int LIKE_COUNT_DELTA_EXPIRE_TIME = 12 * 60 ;

    private static final String LUA_SCRIPT = """
    local delta = redis.call('incrby', KEYS[1], 1)
    redis.call('expire', KEYS[1], tonumber(ARGV[2]))
    if delta >= tonumber(ARGV[1]) then
        return 1
    else
        return 0
    end
    """;

    /**
     * 点赞触发
     * 此方法先取自增后的值，后设置过期时间，判断是否大于阈值。
     * 三个操作非原子性
     * 可能将delta导致重复写库
     * @param postId
     */
    @Deprecated
    public void incrementLikeCount0(Long postId) {
        if(postId == null){
            return;
        }
        String likeDeltaKey = "post:" + postId + ":like_count_delta";// 缓存增量
        // 此方法将key自增1，若key不存在则设置key，基本不存在返回为空问题
        // 返回值为自增后的键值
        Long delta = redisTemplate.opsForValue().increment(likeDeltaKey, 1);
        if(delta == null){
            return;
        }
        redisTemplate.expire(likeDeltaKey, LIKE_COUNT_DELTA_EXPIRE_TIME, TimeUnit.SECONDS);
        // 增量达到50时，更新到数据库
        if (delta >= LIKE_COUNT_DELTA) {
            redisToMysql(likeDeltaKey, postId, Math.toIntExact(delta));
        }
    }
    /**
     * 点赞触发
     * @param postId
     */
    @Override
    public void incrementLikeCount(Long postId) {
        if (postId == null) {
            return;
        }
        String likeDeltaKey = "post:" + postId + ":like_count_delta";
        String lockKey = "lock:post:" + postId;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(LUA_SCRIPT);
        redisScript.setResultType(Long.class);

        Long shouldSync = redisTemplate.execute(
                redisScript,
                Collections.singletonList(likeDeltaKey),
                LIKE_COUNT_DELTA,
                LIKE_COUNT_DELTA_EXPIRE_TIME);
        if (shouldSync == 1L) {
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                try {
                    Integer likeCountDelta = (Integer) redisTemplate.opsForValue().get(likeDeltaKey);
                    if (likeCountDelta != null) {
                        redisToMysql(likeDeltaKey, postId, likeCountDelta);
                    }
                } finally {
                    redisTemplate.delete(lockKey);
                }
            }
        }
    }

    /**
     * 获取点赞数
     * @param postId
     * @return
     */
    @Override
    public Integer getLikeCount(Long postId) {
        if(postId == null){
            return null;
        }
        String likeDeltaKey = "post:" + postId + ":like_count_delta";// 缓存增量
        String totalLikeKey = "post:" + postId + ":like_count";// 缓存总点赞数，
        // 弱一致性关键点：
        // 获取Redis中总点赞数，有则直接返回，无则继续
        Integer totalLike = (Integer) redisTemplate.opsForValue().get(totalLikeKey);
        if (totalLike != null) {
            return totalLike;
        }
        // 获取Redis中的增量（post:1:like_count_delta）
        Integer delta = (Integer) redisTemplate.opsForValue().get(likeDeltaKey);
        if (delta == null) {
            delta = 0;
        }
        // 获取数据库中点赞数
        Posts posts = postsMapper.selectById(postId);
        Integer likeCount = posts.getLikeCount() == null ? 0 : posts.getLikeCount();
        // 将总点赞数写入Redis，计算方法：数据库点赞数+Redis增量数
        // 思考 ：既然每50次点赞或每10分钟触发一次写库，
        // 那么我缓存总点赞量的时候应该设置多少过期时间？
        // 还是就设置成永久而等待其他方法删除？
        redisTemplate.opsForValue().set(
                totalLikeKey, likeCount + delta,LIKE_COUNT_DELTA_EXPIRE_TIME, TimeUnit.SECONDS);

        // 返回 数据库点赞数+Redis增量数
        return likeCount + delta;

    }

    /**
     * 每10分钟将redis中点赞增量同步到数据库
     */
    @Scheduled(fixedRate = LIKE_COUNT_UPDATE_INTERVAL) //每10分钟，更新到数据库
    public void syncLikeCountsToMysql() {
        //拿到每个帖子点赞增量的键
        Set<String> keys = redisTemplate.keys("post:*:like_count_delta");
        if (keys.isEmpty()) return;
        for (String key : keys) {
            // 从键中提取帖子ID
            String postIdStr = key.split(":")[1];
            Long postId = Long.parseLong(postIdStr);
            // 得到点赞增量
            Integer likeCountDelta = (Integer) redisTemplate.opsForValue().get(key);
            // 有id，有增量，就写库，然后清空增量，更新总量
            if (likeCountDelta != null) {
                String lockKey = "lock:post:" + postId;
                Boolean locked = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS);
                if (Boolean.TRUE.equals(locked)) {
                    try {
                        redisToMysql(key, postId, likeCountDelta);
                    } finally {
                        redisTemplate.delete(lockKey);
                    }
                }
            }
        }
    }

    /**
     * 同步方法
     * @param key
     * @param postId
     * @param likeCountDelta
     */
    private void redisToMysql(String key, Long postId, Integer likeCountDelta) {
        String totalLikeKey= "post:" + postId + ":like_count";
        UpdateWrapper<Posts> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", postId)
                .setSql("like_count = like_count + " + likeCountDelta);
        postsMapper.update(updateWrapper);
        //redisTemplate.opsForValue().set(key, 0);
        redisTemplate.delete(key);// 重置缓存
        Integer newLikeCount = postsMapper.selectById(postId).getLikeCount();
        redisTemplate.opsForValue().set(
                totalLikeKey, newLikeCount,LIKE_COUNT_DELTA_EXPIRE_TIME, TimeUnit.SECONDS);
    }
}




