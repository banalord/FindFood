package com.kuang.cachestudy.control;


import com.kuang.cachestudy.pojo.Tiezi0;
import com.kuang.cachestudy.service.Tiezi0Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Tiezi0Controller {

    @Autowired
    private Tiezi0Service tiezi0Service;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @GetMapping("/getTiezi0")
    @Cacheable(value = "AllTiezi")
    public List<Tiezi0> getTiezi0(){
        List<Tiezi0> list = tiezi0Service.list();
        return list;
    }

    @GetMapping("/getTiezi")
    public List<Tiezi0> getTiezi(){
        String key = "AllTiezi";
        List<Tiezi0> AllTieziCache = (List<Tiezi0>) redisTemplate.opsForValue().get(key);
        if(AllTieziCache!=null){
            return AllTieziCache;
        }else{
            List<Tiezi0> list = tiezi0Service.list();
            redisTemplate.opsForValue().set(key,list);
            return list;
        }
    }

    @GetMapping("/getTiezi/{from}/{to}")
    public List<Tiezi0> getTiezi(@PathVariable int from,@PathVariable int to){
        List<Tiezi0> tieziFromRange = tiezi0Service.getTieziFromRange(from, to);
        return tieziFromRange;
    }
}
