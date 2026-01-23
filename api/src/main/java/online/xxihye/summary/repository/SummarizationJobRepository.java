package online.xxihye.summary.repository;


import online.xxihye.summary.domain.SummarizationJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummarizationJobRepository extends JpaRepository<SummarizationJob, Long> {
}
