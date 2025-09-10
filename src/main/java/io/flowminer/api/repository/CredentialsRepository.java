package io.flowminer.api.repository;

import io.flowminer.api.model.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CredentialsRepository extends JpaRepository<Credentials, UUID> {
    List<Credentials> findAllByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Credentials> findByUserIdAndName(String userId, String name);
}
