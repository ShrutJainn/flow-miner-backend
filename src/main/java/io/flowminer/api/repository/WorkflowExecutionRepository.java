package io.flowminer.api.repository;

import io.flowminer.api.enums.WorkflowExecutionStatus;
import io.flowminer.api.model.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {
    List<WorkflowExecution> findByWorkflowIdAndUserIdOrderByCreatedAtDesc(UUID workflowId, String userId);

    @Query("SELECT MIN(w.startedAt) FROM WorkflowExecution w WHERE w.userId = :userId")
    LocalDateTime findMinStartedAtByUserId(@Param("userId") String userId);

    @Query("SELECT w FROM WorkflowExecution w " +
            "WHERE w.userId = :userId " +
            "AND w.startedAt BETWEEN :startDate AND :endDate " +
            "AND w.status IN :statuses")
    List<WorkflowExecution> findByUserIdAndDateRangeAndStatuses(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("statuses") List<WorkflowExecutionStatus> statuses
    );

    @Query("SELECT w FROM WorkflowExecution w " +
            "WHERE w.userId = :userId " +
            "AND w.startedAt >= :startDate " +
            "AND w.startedAt <= :endDate")
    List<WorkflowExecution> findAllByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
