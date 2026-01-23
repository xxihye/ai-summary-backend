package online.xxihye.worker;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.xxihye.summary.repository.SummarizationJobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private static final String STREAM_KEY = "stream:sum:jobs";
    private static final String GROUP = "sum-workers";
    private static final String CONSUMER = "worker-1";

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
            // 그룹이 없으면 생성. stream이 없어도 MKSTREAM처럼 만들기 위해 add로 시드 넣는 것 대신
            // createGroup에 ReadOffset.latest() 사용, stream이 없을 때 예외가 날 수 있음 -> 아래에서 처리
            redis.opsForStream()
                 .createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);
        } catch (Exception ignore) {
            // 이미 그룹 존재하거나, stream이 아직 없어서 실패한 경우 등
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
                // 너무 빡세게 재시도하지 않게
                sleep(300);
            }
        }
    }

    private List<MapRecord<String, Object, Object>> readOnce() {
        StreamReadOptions options = StreamReadOptions.empty()
                                                     .count(10)
                                                     .block(Duration.ofSeconds(2));

        Consumer consumer = Consumer.from(GROUP, CONSUMER);

        // ">" : 그룹에 아직 전달되지 않은 신규 메시지
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
            Object jobIdObj = value.get("jobId");

            if (jobIdObj == null) {
                ack(record);
                return;
            }

            Long jobId = Long.valueOf(jobIdObj.toString());
            jobProcessor.process(jobId);


            ack(record);
        } catch (Exception e) {
            e.printStackTrace();
            // 처리 실패 시 ACK 안 하면, 나중에 PEL(미처리 목록)에서 재처리 가능
            // MVP에서는 일단 ACK 안 하고 넘어가도 됨.
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
