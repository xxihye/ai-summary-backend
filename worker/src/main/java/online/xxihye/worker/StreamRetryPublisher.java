package online.xxihye.worker;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StreamRetryPublisher {

    private final RedisTemplate<String, String> redis;

    @Value("${worker.stream-key}")
    private String streamKey;

    @Value("${worker.job-key}")
    private String valueKey;

    @Value("${worker.attempt-key}")
    private String attemptKey;

    public StreamRetryPublisher(@Qualifier("redisTemplate") RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    public void requeue(Long jobId, int nextAttempt) {
        backoff(nextAttempt);

        redis.opsForStream().add(
            streamKey,
            Map.of(
                valueKey, jobId.toString(),
                attemptKey, String.valueOf(nextAttempt)
            )
        );
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(200L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
