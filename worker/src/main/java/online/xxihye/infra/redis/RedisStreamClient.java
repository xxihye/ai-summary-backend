package online.xxihye.infra.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RedisStreamClient {

    private static final String MKSTREAM_FIELD = "_init";
    private static final String MKSTREAM_VALUE = "1";

    private final RedisTemplate<String, String> redisTemplate;


    public RedisStreamClient(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void ensureGroup(String streamKey, String group) {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.latest(), group);
            log.info("stream group created. stream={}, group={}", streamKey, group);
        } catch (Exception e) {
            if (isBusyGroup(e)) {
                log.info("stream group already exists. stream={}, group={}", streamKey, group);
                return;
            }

            if (isNoSuchKey(e)) {
                createStreamIfMissing(streamKey);
                try {
                    redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.latest(), group);
                    log.info("stream group created after mkstream. stream={}, group={}", streamKey, group);
                    return;
                } catch (Exception retryEx) {
                    if (isBusyGroup(retryEx)) {
                        log.info("stream group already exists after mkstream. stream={}, group={}", streamKey, group);
                        return;
                    }
                    log.warn("failed to create stream group after mkstream. stream={}, group={}", streamKey, group, retryEx);
                    throw retryEx;
                }
            }

            log.warn("failed to create stream group. stream={}, group={}", streamKey, group, e);
            throw e;
        }
    }

    private boolean isNoSuchKey(Throwable e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        String m = msg.toLowerCase();
        return m.contains("no such key") || m.contains("nkey") || m.contains("key does not exist");
    }

    private void createStreamIfMissing(String streamKey) {
        redisTemplate.opsForStream()
                     .add(
                         streamKey,
                         Collections.singletonMap(MKSTREAM_FIELD, MKSTREAM_VALUE)
                     );
        log.info("stream created (mkstream-like). stream={}", streamKey);
    }

    //존재하는 그룹으로 인한 Exception 인지 확인
    private boolean isBusyGroup(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof io.lettuce.core.RedisBusyException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    public List<MapRecord<String, Object, Object>> readOnce(
        String streamKey,
        String group,
        String consumerName,
        int count,
        Duration block
    ) {
        StreamReadOptions options = StreamReadOptions.empty()
                                                     .count(count)
                                                     .block(block);

        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                                                                       .read(
                                                                           Consumer.from(group, consumerName),
                                                                           options,
                                                                           StreamOffset.create(streamKey, ReadOffset.lastConsumed())
                                                                       );

        return records == null ? List.of() : records;
    }


    public long ack(String streamKey, String group, RecordId recordId) {
        Long acked = redisTemplate.opsForStream()
                                  .acknowledge(streamKey, group, recordId);
        return acked == null ? 0L : acked;
    }

}
