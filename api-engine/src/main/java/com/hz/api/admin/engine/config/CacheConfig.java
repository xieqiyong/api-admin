package com.hz.api.admin.engine.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * @author xieqiyong66@gmail.com
 * @description: 用于模拟redisson锁场景
 * @date 2022/4/29 5:32 下午
 */
@Configuration
public class CacheConfig {

    public static final  String CLIENT_CHANNEL_CACHE = "CLIENT_CHANNEL_CACHE";


    @Bean("localCache")
    public Cache<String, Object> getLocalConfig() {
        return Caffeine.newBuilder()
                // 初始的缓存空间大小
                .initialCapacity(1024)
                // 缓存的最大条数
                .maximumSize(1000)
                .build();
    }
}
