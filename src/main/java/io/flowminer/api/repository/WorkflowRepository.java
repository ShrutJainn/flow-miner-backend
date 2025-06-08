package io.flowminer.api.repository;

import io.flowminer.api.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    public List<Workflow> findByUserIdOrderByCreatedAtAsc(String userId);

    public Optional<Workflow> findByIdAndUserId(UUID id, String userId);

    public Workflow findByIdAndLastRunId(UUID id, String lastRunId);
}
