package io.flowminer.api.repository;

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
}
