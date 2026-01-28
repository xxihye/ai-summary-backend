package online.xxihye.summary.repository;


import online.xxihye.summary.domain.SummarizationJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummarizationJobRepository extends JpaRepository<SummarizationJob, Long> {

    Optional<SummarizationJob> findByIdAndUserNo(Long id, Long userNo);
}
