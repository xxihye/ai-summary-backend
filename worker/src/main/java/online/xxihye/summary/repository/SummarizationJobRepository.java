package online.xxihye.summary.repository;

import online.xxihye.summary.domain.JobErrorCode;
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update SummarizationJob j
           set j.status = :toStatus,
               j.resultId = :resultId,
               j.model = :model,
               j.finishedAt = :finishedAt,
               j.updatedAt = :updatedAt
         where j.id = :id
           and j.status = :fromStatus
    """)
    int markSuccessIfRunning(
        @Param("id") Long id,
        @Param("fromStatus") JobStatus fromStatus,
        @Param("toStatus") JobStatus toStatus,
        @Param("resultId") Long resultId,
        @Param("model") String model,
        @Param("finishedAt") LocalDateTime finishedAt,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update SummarizationJob j
           set j.status = :toStatus,
               j.errorCode = :errorCode,
               j.errorMessage = :errorMessage,
               j.model = :model,
               j.finishedAt = :finishedAt,
               j.updatedAt = :updatedAt
         where j.id = :id
           and j.status in :fromStatuses
    """)
    int markFailedIfPendingOrRunning(
        @Param("id") Long id,
        @Param("fromStatuses") java.util.Collection<JobStatus> fromStatuses,
        @Param("toStatus") JobStatus toStatus,
        @Param("errorCode") JobErrorCode errorCode,
        @Param("errorMessage") String errorMessage,
        @Param("model") String model,
        @Param("finishedAt") java.time.LocalDateTime finishedAt,
        @Param("updatedAt") java.time.LocalDateTime updatedAt
    );

}
