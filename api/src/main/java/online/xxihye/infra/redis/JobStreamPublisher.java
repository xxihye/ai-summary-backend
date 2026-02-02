package online.xxihye.infra.redis;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JobStreamPublisher {

    private static final String STREAM_KEY = "stream:sum:jobs";

    private final RedisTemplate<String, String> redisTemplate;

    public JobStreamPublisher(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RecordId publish(Long jobId) {
        Map<String, String> fields = Map.of("jobId", String.valueOf(jobId));

        ObjectRecord<String, Map<String, String>> record =
            StreamRecords.newRecord()
                         .ofObject(fields)
                         .withStreamKey(STREAM_KEY);

        return redisTemplate.opsForStream().add(record);
    }
}
