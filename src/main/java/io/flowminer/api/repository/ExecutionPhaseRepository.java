package io.flowminer.api.repository;

import io.flowminer.api.model.ExecutionPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionPhaseRepository extends JpaRepository<ExecutionPhase, UUID> {
    List<ExecutionPhase> findAllByWorkflowExecutionId(UUID workflowExecutionId);
}
