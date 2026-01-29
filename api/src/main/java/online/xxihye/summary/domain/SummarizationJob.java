package online.xxihye.summary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "summarization_jobs")
public class SummarizationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    @Column(name = "input_hash", nullable = false, length = 64)
    private String inputHash;

    @Column(name = "input_text_len", nullable = false)
    private Integer inputTextLen;

    @Column(length = 50)
    private String model;

    @Column(name = "prompt_version", nullable = false, length = 20)
    private String promptVersion = "v1";

    @Column(name = "cache_hit", nullable = false)
    private Boolean cacheHit = false;

    @Column(name = "result_id")
    private Long resultId;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_code", length = 50)
    private JobErrorCode errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected SummarizationJob() {
    }

    public SummarizationJob(Long userNo, JobStatus status, String inputHash, int inputTextLen, String promptVersion, boolean cacheHit, Long resultId, LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.userNo = userNo;
        this.status = status;
        this.inputHash = inputHash;
        this.inputTextLen = inputTextLen;
        this.promptVersion = promptVersion;
        this.cacheHit = cacheHit;
        this.resultId = resultId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public Long getId() {
        return id;
    }

    public JobStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getResultId() {
        return this.resultId;
    }

    public String getModel() {
        return this.model;
    }

    public String getPromptVersion() {
        return this.promptVersion;
    }

    public Boolean getCacheHit() {
        return cacheHit;
    }

    public static SummarizationJob createPending(Long userNo, String inputHash, int inputTextLen, String promptVersion) {
        return new SummarizationJob(userNo, JobStatus.PENDING, inputHash, inputTextLen, promptVersion, false, null, null, null);
    }

    public static SummarizationJob createSuccessFromCache(Long userNo, String inputHash, int inputTextLen, String promptVersion, Long resultId, LocalDateTime now) {
        return new SummarizationJob(userNo, JobStatus.SUCCESS, inputHash, inputTextLen, promptVersion, true, resultId, now, now);
    }
}
