package online.xxihye.infra.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class UserQuotaClient {

    private final RedisTemplate<String, String> redisTemplate;

    public UserQuotaClient(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long increment(String key) {
        Long v = redisTemplate.opsForValue().increment(key);
        if (v == null) {
            throw new IllegalStateException("redis INCR returned null");
        }
        return v;
    }

    /**
     * TTL is only set when key is first created (count==1) to avoid resetting TTL.
     */
    public void expireIfFirstHit(String key, long count, Duration ttl) {
        if (count == 1L) {
            redisTemplate.expire(key, ttl);
        }
    }
}
