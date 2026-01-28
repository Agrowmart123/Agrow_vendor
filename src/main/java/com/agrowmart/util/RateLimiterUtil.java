package com.agrowmart.util;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterUtil {
    @Autowired private RedisTemplate<String, String> redisTemplate;

    public boolean isAllowed(String key, long maxRequests, long windowSeconds) {
        String rateKey = "rate:" + key;
        Long count = redisTemplate.opsForValue().increment(rateKey);
        if (count == 1) redisTemplate.expire(rateKey, windowSeconds, TimeUnit.SECONDS);
        return count <= maxRequests;
    }
}