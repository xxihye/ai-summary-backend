package online.xxihye.worker;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import online.xxihye.infra.redis.RedisStreamClient;
import online.xxihye.infra.redis.StreamRetryPublisher;
import online.xxihye.summary.domain.JobErrorCode;
import online.xxihye.worker.exception.WorkerException;
import online.xxihye.worker.service.JobTransitionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
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

    private final JobProcessor jobProcessor;
    private final JobTransitionService transitionService;
    private final RedisStreamClient redisStreamClient;
    private final StreamRetryPublisher retryPublisher;


    public SummarizationStreamWorker(JobProcessor jobProcessor,
                                     JobTransitionService transitionService1, RedisStreamClient redisStreamClient,
                                     StreamRetryPublisher retryPublisher
    ) {
        this.jobProcessor = jobProcessor;
        this.transitionService = transitionService1;
        this.redisStreamClient = redisStreamClient;
        this.retryPublisher = retryPublisher;
    }

    @PostConstruct
    public void init() {
        redisStreamClient.ensureGroup(STREAM_KEY, GROUP);
    }

    @Override
    public void run(String... args) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<MapRecord<String, Object, Object>> records = redisStreamClient.readOnce(
                    STREAM_KEY,
                    GROUP,
                    CONSUMER,
                    10,
                    Duration.ofSeconds(2)
                );

                for (MapRecord<String, Object, Object> record : records) {
                    handleRecord(record);
                }
            } catch (Exception e) {
                log.error("worker loop error", e.getMessage());
                loopBackoff();
            }
        }
    }

    private void loopBackoff() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {
            Thread.currentThread()
                  .interrupt(); // interrupt 상태 복구
        }
    }

    //메시지 처리
    private void handleRecord(MapRecord<String, Object, Object> record) {
        Object jobIdObj = record.getValue().get(JOB_KEY);
        RecordId recordId = record.getId();

        if (jobIdObj == null) {
            redisStreamClient.ack(STREAM_KEY, GROUP, recordId);
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
            redisStreamClient.ack(STREAM_KEY, GROUP, recordId);
            return;
        }

        try {
            jobProcessor.process(jobId);
            redisStreamClient.ack(STREAM_KEY, GROUP, recordId);
        } catch (WorkerException e) {
            //재시도 대상인 경우, ack 처리 후 queue에 삽입.
            if (e.getErrorCode().isRetryable()) {
                redisStreamClient.ack(STREAM_KEY, GROUP, recordId);
                backoff(++attempt);
                retryPublisher.publish(jobId, attempt);
                log.info("requeue job id : {}, attempt : {}", jobId, attempt);
                return;
            }

            //재시도 대상 아님.
            redisStreamClient.ack(STREAM_KEY, GROUP, recordId);
        } catch (Exception e) {
            //예측못한 오류로 재시도 안함.
            log.error("job process error", e);
            redisStreamClient.ack(STREAM_KEY, GROUP, recordId);
        }
    }

    private int parseAttempt(Object raw) {
        if (raw == null) {
            return 0;
        }

        return Integer.parseInt(raw.toString());
    }

    private void backoff(int attempt) {
        try { Thread.sleep(200L * attempt); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

}
