package com.kuang.cachestudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CacheStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheStudyApplication.class, args);
    }

}
