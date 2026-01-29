package online.xxihye.summary.repository;

import online.xxihye.summary.domain.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Optional<Summary> findByInputHash(String inputHash);
}
