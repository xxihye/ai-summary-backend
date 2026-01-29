package online.xxihye.summary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "summarization_inputs")
public class SummarizationInput {

    @Id
    @Column(name = "job_id")
    private Long jobId;

    @Lob
    @Column(name = "input_text", nullable = false)
    private String inputText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SummarizationInput() {}

    public String getInputText(){
        return inputText;
    }
}
