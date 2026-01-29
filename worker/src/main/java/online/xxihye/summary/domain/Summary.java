package online.xxihye.summary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "summaries",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_summaries_input_hash", columnNames = "input_hash")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "input_hash", nullable = false, length = 64)
    private String inputHash;

    @Lob
    @Column(name = "summary_text", nullable = false)
    private String summaryText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keywords_json", columnDefinition = "json")
    private String keywordsJson;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Summary() {}

    protected Summary(String inputHash, String summaryText, String keywordsJson) {
        this.inputHash = inputHash;
        this.summaryText = summaryText;
        this.keywordsJson = keywordsJson;
    }

    public static Summary createSummary(String inputHash, String summaryText){
        return new Summary(inputHash, summaryText, null);
    }

    public String getSummaryText() {
        return summaryText;
    }

    public Long getId(){
        return id;
    }
}
