package online.xxihye.summary.repository;

import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.domain.SummarizationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SummarizationJobRepository extends JpaRepository<SummarizationJob, Long> {

    @Modifying
    @Query("""
        update SummarizationJob j
           set j.status = :toStatus,
               j.startedAt = :startedAt,
               j.updatedAt = :updatedAt
         where j.id = :id
           and j.status = :fromStatus
    """)
    int moveToRunningIfPending(
        @Param("id") Long id,
        @Param("fromStatus") JobStatus fromStatus,
        @Param("toStatus") JobStatus toStatus,
        @Param("startedAt") LocalDateTime startedAt,
        @Param("updatedAt") LocalDateTime updatedAt
    );
}
