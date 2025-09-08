package io.flowminer.api.repository;

import io.flowminer.api.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
     List<Workflow> findByUserIdOrderByCreatedAtAsc(String userId);

     Optional<Workflow> findByIdAndUserId(UUID id, String userId);

     Workflow findByIdAndLastRunId(UUID id, String lastRunId);

     List<Workflow> findByNextRunAtBefore(LocalDateTime now);
}
