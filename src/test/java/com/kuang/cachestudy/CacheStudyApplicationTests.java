//package com.kuang.cachestudy;
//
//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.TypeReference;
//import com.kuang.cachestudy.pojo.Tiezi0;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//@SpringBootTest
//class CacheStudyApplicationTests {
//
//    @Autowired
//    RedisTemplate redisTemplate;
//
//    @Autowired
//    JdbcTemplate jdbcTemplate;
//
//    @Test
//    void contextLoads() {
//        List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from tiezi0 where id between 10 and 20");
//        String json = JSON.toJSONString(list);
//        redisTemplate.opsForValue().set("tiezi",json);
//        String tiezi = (String) redisTemplate.opsForValue().get("tiezi");
//        System.out.println(tiezi);
//    }
//
//}
