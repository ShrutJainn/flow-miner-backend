package io.flowminer.api.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveEnvironmentOnRedis(String key, Object environment) {
        redisTemplate.opsForValue().set(key, environment, Duration.ofMinutes(30));
    }

    public String getEnvironment(String key) {
        return Objects.requireNonNull(redisTemplate.opsForValue().get(key)).toString();
    }
}
