package com.minimall.auth.jwt;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final StringRedisTemplate redis;

    public RedisRefreshTokenStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String key(Long memberId) {
        return "rt:" + memberId;
    }

    @Override
    public void save(Long memberId, String refreshJti, Duration ttl) {
        redis.opsForValue().set(key(memberId), refreshJti, ttl);
    }

    @Override
    public String findJti(Long memberId) {
        return redis.opsForValue().get(key(memberId));
    }

    @Override
    public void delete(Long memberId) {
        redis.delete(key(memberId));
    }
}
