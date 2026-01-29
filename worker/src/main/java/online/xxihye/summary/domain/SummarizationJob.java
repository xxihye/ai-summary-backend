package online.xxihye.summary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "summarization_jobs")
public class SummarizationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_no", nullable=false)
    private Long userNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private JobStatus status;

    @Column(name="input_hash", nullable=false, length=64)
    private String inputHash;

    @Column(name="input_text_len", nullable=false)
    private Integer inputTextLen;

    @Column(length=50)
    private String model;

    @Column(name="prompt_version", nullable=false, length=20)
    private String promptVersion;

    @Column(name="cache_hit", nullable=false)
    private Boolean cacheHit;

    @Column(name="result_id")
    private Long resultId;

    @Enumerated(EnumType.STRING)
    @Column(name="error_code", length=50)
    private JobErrorCode errorCode;

    @Column(name="error_message", length=500)
    private String errorMessage;

    @Column(name="started_at")
    private LocalDateTime startedAt;

    @Column(name="finished_at")
    private LocalDateTime finishedAt;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    protected SummarizationJob() {}

    public JobStatus getStatus() { return status; }

    public String getInputHash(){
        return inputHash;
    }
}
