package online.xxihye.worker;

import jakarta.annotation.PostConstruct;
import online.xxihye.summary.repository.SummarizationJobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;


@Component
public class SummarizationStreamWorker implements CommandLineRunner {

    @Value("${worker.stream-key}")
    private String STREAM_KEY;

    @Value("${worker.group-name}")
    private String GROUP;

    @Value("${worker.consumer-name}")
    private String CONSUMER;

    @Value("${worker.value-key}")
    private String VALUE_KEY;

    private final RedisTemplate<String, String> redis;
    private final SummarizationJobRepository repo;
    private final JobProcessor jobProcessor;

    public SummarizationStreamWorker(@Qualifier("redisTemplate") RedisTemplate<String, String> redis,
                                     SummarizationJobRepository repo,
                                     JobProcessor jobProcessor) {
        this.redis = redis;
        this.repo = repo;
        this.jobProcessor = jobProcessor;
    }

    @PostConstruct
    public void ensureGroup() {
        try {
            redis.opsForStream()
                 .createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    @Override
    public void run(String... args) {
        while (true) {
            try {
                List<MapRecord<String, Object, Object>> records = readOnce();

                for (MapRecord<String, Object, Object> record : records) {
                    handleRecord(record);
                }
            } catch (Exception e) {
                sleep(300);
            }
        }
    }

    private List<MapRecord<String, Object, Object>> readOnce() {
        StreamReadOptions options = StreamReadOptions.empty()
                                                     .count(10)
                                                     .block(Duration.ofSeconds(2));

        Consumer consumer = Consumer.from(GROUP, CONSUMER);

        return redis.opsForStream()
                    .read(
                        consumer,
                        options,
                        StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
                    );
    }

    private void handleRecord(MapRecord<String, Object, Object> record) {
        try {
            Map<Object, Object> value = record.getValue();
            Object jobIdObj = value.get(VALUE_KEY);

            //jobId 값이 없는 경우, ack 처리
            if (jobIdObj == null) {
                ack(record);
                return;
            }

            //처리후 ack 처리
            Long jobId = Long.valueOf(jobIdObj.toString());
            jobProcessor.process(jobId);
            ack(record);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO : 실패 시 ACK 안 하고 PEL에서 재처리
        }
    }

    private void ack(MapRecord<String, Object, Object> record) {
        redis.opsForStream()
             .acknowledge(STREAM_KEY, GROUP, record.getId());
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            ignored.printStackTrace();
        }
    }
}
