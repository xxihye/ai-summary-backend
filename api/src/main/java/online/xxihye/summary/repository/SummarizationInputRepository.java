package online.xxihye.summary.repository;

import online.xxihye.summary.domain.SummarizationInput;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummarizationInputRepository extends JpaRepository<SummarizationInput, Long> {
    Optional<SummarizationInput> findByJobId(Long jobId);
}
