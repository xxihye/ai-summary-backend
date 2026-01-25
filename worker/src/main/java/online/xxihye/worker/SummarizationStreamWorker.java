package online.xxihye.worker;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import online.xxihye.summary.domain.JobErrorCode;
import online.xxihye.summary.repository.SummarizationJobRepository;
import online.xxihye.worker.exception.AiProcessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
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
    private final SummarizationJobRepository repository;
    private final JobProcessor jobProcessor;

    public SummarizationStreamWorker(@Qualifier("redisTemplate") RedisTemplate<String, String> redis,
                                     SummarizationJobRepository repository,
                                     JobProcessor jobProcessor) {
        this.redis = redis;
        this.repository = repository;
        this.jobProcessor = jobProcessor;
    }

    @PostConstruct
    public void ensureGroup() {
        try {
            redis.opsForStream()
//                 .createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);
                 .createGroup(STREAM_KEY, ReadOffset.from("0-0"), GROUP);
            log.info("stream group created. stream={}, group={}", STREAM_KEY, GROUP);
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();

            if (msg.contains("BUSYGROUP")) {
                log.info("stream group already exists. stream={}, group={}", STREAM_KEY, GROUP);
                return;
            }

            log.warn("failed to create stream group. stream={}, group={}", STREAM_KEY, GROUP);
        }
    }

    @Override
    public void run(String... args) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<MapRecord<String, Object, Object>> records = readOnce();

                for (MapRecord<String, Object, Object> record : records) {
                    handleRecord(record);
                }
            } catch (Exception e) {
                log.error("worker loop error", e.getMessage());
                loopBackoff();
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
        long deliveryCount = getDeliveryCount(record);

        if (deliveryCount > 3) {
            Long jobId = Long.valueOf(record.getValue()
                                            .get(VALUE_KEY)
                                            .toString());
            log.warn("retry exceeded jobId={} delivery={}", jobId, deliveryCount);

            markFailedRetryExceeded(jobId);
            ack(record);
            return;
        }

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
        } catch(AiProcessException e){
            //재시도 대상
            if(isRetryable(e.getErrorCode())){
                return;
            }

            //재시도 대상 아님.
            ack(record);
        } catch (Exception e) {
            //예측못한 오류로 재시도 안함.
            log.error("job process error", e.getMessage());
        }
    }

    private long getDeliveryCount(MapRecord<String, Object, Object> record) {
        String entryId = record.getId()
                               .getValue();

        PendingMessages pending = redis.opsForStream()
                                       .pending(STREAM_KEY, GROUP, Range.closed(entryId, entryId), 1);

        if (pending.isEmpty()) {
            return 1L; // 최초 전달
        }

        PendingMessage pm = pending.get(0);
        return pm.getTotalDeliveryCount();
    }

    @Transactional
    public void markFailedRetryExceeded(Long jobId) {
        repository.findById(jobId)
                  .ifPresent(job ->
                      job.markFailed(
                          JobErrorCode.RETRY_EXCEEDED,
                          "retry count exceeded", null
                      )
                  );
    }

    private void ack(MapRecord<String, Object, Object> record) {
        redis.opsForStream()
             .acknowledge(STREAM_KEY, GROUP, record.getId());
    }

    private boolean isRetryable(JobErrorCode code) {
        return code == JobErrorCode.AI_RATE_LIMITED
            || code == JobErrorCode.AI_TIMEOUT
            || code == JobErrorCode.AI_UNAVAILABLE;
    }

    private void loopBackoff() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // interrupt 상태 복구
        }
    }
}
