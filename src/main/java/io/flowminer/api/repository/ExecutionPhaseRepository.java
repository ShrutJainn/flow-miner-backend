package io.flowminer.api.repository;

import io.flowminer.api.model.ExecutionPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionPhaseRepository extends JpaRepository<ExecutionPhase, UUID> {
    List<ExecutionPhase> findAllByWorkflowExecutionId(UUID workflowExecutionId);

    @Query("SELECT e FROM ExecutionPhase e " +
            "WHERE e.workflowExecutionId IN :executionIds " +
            "AND e.creditsCost IS NOT NULL")
    List<ExecutionPhase> findByExecutionIdsWithCredits(@Param("executionIds") List<UUID> executionIds);
}
