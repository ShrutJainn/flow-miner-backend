package io.flowminer.api.repository;

import io.flowminer.api.model.EnvironmentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnvironmentRepository extends JpaRepository<EnvironmentModel, UUID> {
    Optional<EnvironmentModel> findByWorkflowId(UUID workflowId);
}
