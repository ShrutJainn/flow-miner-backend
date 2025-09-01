package io.flowminer.api.repository;

import io.flowminer.api.model.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {
    List<WorkflowExecution> findByWorkflowIdAndUserIdOrderByCreatedAtDesc(UUID workflowId, String userId);
}
