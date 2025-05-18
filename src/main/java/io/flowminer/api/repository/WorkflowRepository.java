package io.flowminer.api.repository;

import io.flowminer.api.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    public List<Workflow> findByUserIdOrderByCreatedAtAsc(String userId);
}
