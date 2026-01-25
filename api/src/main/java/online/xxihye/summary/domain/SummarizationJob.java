package online.xxihye.summary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "summarization_jobs")
@DynamicUpdate
public class SummarizationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private JobStatus status;

    @Lob
    @Column(name = "input_text", columnDefinition = "TEXT", nullable = false)
    private String inputText;

    @Column(name="input_hash", nullable=false, length=64)
    private String inputHash;

    @Column(name="input_text_len", nullable=false)
    private Integer inputTextLen;

    @Column(length=50)
    private String model;

    @Column(name="prompt_version", nullable=false, length=20)
    private String promptVersion = "v1";

    @Column(name="cache_hit", nullable=false)
    private Boolean cacheHit = false;

    @Lob
    @Column(name="result_text")
    private String resultText;

    @Enumerated(EnumType.STRING)
    @Column(name="error_code", length=50)
    private JobErrorCode errorCode;

    @Column(name="error_message", length=500)
    private String errorMessage;

    @Column(name="started_at")
    private LocalDateTime startedAt;

    @Column(name="finished_at")
    private LocalDateTime finishedAt;

    @CreatedDate
    @Column(name="created_at", nullable=false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    protected SummarizationJob() {}

    public SummarizationJob(Long userId, String text, String inputHash, int inputTextLen) {
        this.userId = userId;
        this.inputText = text;
        this.status = JobStatus.PENDING;
        this.inputHash = inputHash;
        this.inputTextLen = inputTextLen;
        this.promptVersion = "v1";
        this.cacheHit = false;
    }

    public Long getId() {
        return id;
    }

    public String getInputText() {
        return inputText;
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

    public String getResultText() {
        return resultText;
    }

    public void markRunning() {
        this.status = JobStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void markSuccess(String resultText) {
        this.status = JobStatus.SUCCESS;
        this.resultText = resultText;
        this.finishedAt = LocalDateTime.now();
    }

    public void markFailed(JobErrorCode errorCode, String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.finishedAt = LocalDateTime.now();
    }
}
