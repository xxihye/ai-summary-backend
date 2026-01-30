package online.xxihye.summary.repository;

import online.xxihye.summary.domain.JobErrorCode;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.domain.SummarizationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SummarizationJobRepository extends JpaRepository<SummarizationJob, Long> {

    @Modifying
    @Query("""
        update SummarizationJob j
           set j.status = :toStatus,
               j.startedAt = coalesce(j.startedAt, :now),
               j.updatedAt = :now
         where j.id = :id
           and j.status in :fromStatuses 
    """)
    int moveToRunning(
        @Param("id") Long id,
        @Param("fromStatuses") List<JobStatus> fromStatuses,
        @Param("toStatus") JobStatus toStatus,
        @Param("now") LocalDateTime now
    );

    @Modifying
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

    @Modifying
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
        @Param("fromStatuses") List<JobStatus> fromStatuses,
        @Param("toStatus") JobStatus toStatus,
        @Param("errorCode") JobErrorCode errorCode,
        @Param("errorMessage") String errorMessage,
        @Param("model") String model,
        @Param("finishedAt") LocalDateTime finishedAt,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying
    @Query("""
        update SummarizationJob j
           set j.status = :toStatus,
               j.errorCode = :errorCode,
               j.errorMessage = :errorMessage,
               j.updatedAt = :now
         where j.id = :jobId
           and j.status = :fromStatus
    """)
    int markRetrying(
        @Param("jobId") Long jobId,
        @Param("fromStatus") JobStatus fromStatus,
        @Param("toStatus") JobStatus toStatus,
        @Param("errorCode") JobErrorCode errorCode,
        @Param("errorMessage") String errorMessage,
        @Param("now") LocalDateTime now
    );

}
