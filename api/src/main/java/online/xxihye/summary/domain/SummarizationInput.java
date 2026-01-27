package online.xxihye.summary.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "summarization_inputs")
@EntityListeners(AuditingEntityListener.class)
public class SummarizationInput {

    @Id
    @Column(name = "job_id")
    private Long jobId;

    @Lob
    @Column(name = "input_text", nullable = false)
    private String inputText;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SummarizationInput() {}

    public SummarizationInput(Long jobId, String inputText) {
        this.jobId = jobId;
        this.inputText = inputText;
    }

    public String getInputText(){
        return inputText;
    }
}
