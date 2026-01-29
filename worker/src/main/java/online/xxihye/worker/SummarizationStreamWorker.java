package online.xxihye.worker;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import online.xxihye.summary.domain.JobErrorCode;
import online.xxihye.worker.exception.AiProcessException;
import online.xxihye.worker.exception.WorkerException;
import online.xxihye.worker.service.JobTransitionService;
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
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class SummarizationStreamWorker implements CommandLineRunner {

    @Value("${worker.stream-key}")
    private String STREAM_KEY;

    @Value("${worker.group-name}")
    private String GROUP;

    @Value("${worker.consumer-name}")
    private String CONSUMER;

    @Value("${worker.job-key}")
    private String JOB_KEY;

    @Value("${worker.attempt-key}")
    private String ATTEMPT_KEY;

    private static final int MAX_ATTEMPTS = 3;

    private final RedisTemplate<String, String> redis;
    private final JobProcessor jobProcessor;
    private final JobTransitionService transitionService;
    private final StreamRetryPublisher retryPublisher;


    public SummarizationStreamWorker(@Qualifier("redisTemplate") RedisTemplate<String, String> redis,
                                     JobProcessor jobProcessor,
                                     JobTransitionService transitionService1,
                                     StreamRetryPublisher retryPublisher
    ) {
        this.redis = redis;
        this.jobProcessor = jobProcessor;
        this.transitionService = transitionService1;
        this.retryPublisher = retryPublisher;
    }

    @PostConstruct
    public void ensureGroup() {
        try {
            redis.opsForStream()
                 .createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);
            log.info("stream group created. stream={}, group={}", STREAM_KEY, GROUP);
        } catch (Exception e) {
            if (isBusyGroup(e)) {
                log.info("stream group already exists. stream={}, group={}", STREAM_KEY, GROUP);
                return;
            }
            log.warn("failed to create stream group. stream={}, group={}", STREAM_KEY, GROUP, e);
        }
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

    //Stream 메시지 읽기
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

    //메시지 처리
    private void handleRecord(MapRecord<String, Object, Object> record) {
        Object jobIdObj = record.getValue().get(JOB_KEY);
        if (jobIdObj == null) {
            ack(record);
            return;
        }

        Long jobId = Long.valueOf(jobIdObj.toString());
        int attempt = parseAttempt(record.getValue().get(ATTEMPT_KEY));

        //최대 시도 이상시 실패처리
        if (attempt >= MAX_ATTEMPTS) {
            int updated = transitionService.markFailed(
                jobId,
                JobErrorCode.RETRY_EXCEEDED,
                "retry count exceeded",
                null,
                LocalDateTime.now()
            );

            log.info("job failed. jobId={}, errorCode={}, updated={}", jobId, JobErrorCode.RETRY_EXCEEDED, updated);
            ack(record);
            return;
        }

        try {
            jobProcessor.process(jobId);
            ack(record);
        } catch (WorkerException e) {
            //재시도 대상인 경우, ack 처리 후 queue에 삽입.
            if (isRetryable(e.getErrorCode())) {
                ack(record);
                retryPublisher.requeue(jobId, attempt + 1);
                return;
            }

            //재시도 대상 아님.
            ack(record);
        } catch (Exception e) {
            //예측못한 오류로 재시도 안함.
            log.error("job process error", e);
            ack(record);
        }
    }

    private void ack(MapRecord<String, Object, Object> record) {
        redis.opsForStream().acknowledge(STREAM_KEY, GROUP, record.getId());
    }

    private int parseAttempt(Object raw) {
        if (raw == null) {
            return 0;
        }

        return Integer.parseInt(raw.toString());
    }

    private boolean isRetryable(JobErrorCode code) {
        return code == JobErrorCode.AI_RATE_LIMITED
            || code == JobErrorCode.AI_INTERNAL_ERROR
            || code == JobErrorCode.AI_SERVICE_UNAVAILABLE
            || code == JobErrorCode.AI_NETWORK_ERROR
            || code == JobErrorCode.AI_TIMEOUT;
    }

    private void loopBackoff() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {
            Thread.currentThread()
                  .interrupt(); // interrupt 상태 복구
        }
    }
}
