package com.kuang.cachestudy.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 设置Key的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        // 设置Value的序列化方式
        template.setValueSerializer(new FastJson2RedisSerializer<>(Object.class));

        // 设置HashKey的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置HashValue的序列化方式
        template.setHashValueSerializer(new FastJson2RedisSerializer<>(Object.class));

        //template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    private static class FastJson2RedisSerializer<T> implements RedisSerializer<T> {

        private Class<T> clazz;

        public FastJson2RedisSerializer(Class<T> clazz) {
            super();
            this.clazz = clazz;
        }

        @Override
        public byte[] serialize(T t) throws SerializationException {
            if (t == null) {
                return new byte[0];
            }
            return JSON.toJSONString(t, JSONWriter.Feature.WriteClassName).getBytes(StandardCharsets.UTF_8);
            // 以下移除了WriteClassName特性，不再在序列化的JSON中包含类型信息
            //return JSON.toJSONString(t).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public T deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            String str = new String(bytes, StandardCharsets.UTF_8);
            return JSON.parseObject(str, clazz);
            // 注意：由于不再包含类型信息，反序列化时务必确保传入正确的类型或进行适当的错误处理
            //return JSON.parseObject(str, clazz);
        }
    }
}
