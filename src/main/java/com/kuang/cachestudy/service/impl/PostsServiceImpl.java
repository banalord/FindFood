package com.kuang.cachestudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.injector.methods.SelectById;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuang.cachestudy.pojo.Posts;
import com.kuang.cachestudy.service.PostsService;
import com.kuang.cachestudy.mapper.PostsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description 针对表【posts】的数据库操作Service实现
 * @createDate 2025-04-07 18:59:53
 */
@Service
public class PostsServiceImpl extends ServiceImpl<PostsMapper, Posts> implements PostsService {
    @Autowired
    private PostsMapper postsMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    //@Override
    //public void updateLikeCount(Long postId, Integer likeCount) {
    //    Posts post = new Posts();
    //    post.setId(postId);
    //    post.setLikeCount(post.getLikeCount()+likeCount);
    //    postsMapper.updateById(post);
    //}

    @Override
    public void incrementLikeCount(Long postId) {
        String likeDeltaKey = "post:" + postId + ":like_count_delta";// 缓存增量
        Integer likeCountDelta = Math.toIntExact(redisTemplate.opsForValue().increment(likeDeltaKey, 1));
        redisTemplate.expire(likeDeltaKey, 12, TimeUnit.MINUTES);
        // 增量达到50时，更新到数据库
        if (likeCountDelta != null && likeCountDelta >= 50) {
            redisToMysql(likeDeltaKey, postId, likeCountDelta);
        }
    }

    @Override
    public Integer getLikeCount(Long postId) {
        String likeDeltaKey = "post:" + postId + ":like_count_delta";// 缓存增量
        String totalLikeKey = "post:" + postId + ":like_count";// 缓存总点赞数，
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
        // 将总点赞数写入Redis
        // 思考 ：既然每50次点赞或每10分钟触发一次写库，
        // 那么我缓存总点赞量的时候应该设置多少过期时间？
        // 还是就设置成永久而等待其他方法删除？
        redisTemplate.opsForValue().set(totalLikeKey, likeCount + delta,12, TimeUnit.MINUTES);

        // 返回 数据库点赞数+Redis增量数
        return likeCount + delta;

    }

    @Scheduled(fixedRate = 10 * 60 * 1000) //每10分钟，更新到数据库
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
                redisToMysql(key, postId, likeCountDelta);
            }
        }
    }

    private void redisToMysql(String key, Long postId, Integer likeCountDelta) {
        String totalLikeKey= "post:" + postId + ":like_count";
        UpdateWrapper<Posts> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", postId)
                .setSql("like_count = like_count + " + likeCountDelta);
        postsMapper.update(updateWrapper);
        redisTemplate.opsForValue().set(key, 0); // 重置缓存
        Integer newLikeCount = postsMapper.selectById(postId).getLikeCount();
        redisTemplate.opsForValue().set(totalLikeKey, newLikeCount,12, TimeUnit.MINUTES);
    }
}




