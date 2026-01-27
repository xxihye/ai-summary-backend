package online.xxihye.summary.repository;

import online.xxihye.summary.domain.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
}
