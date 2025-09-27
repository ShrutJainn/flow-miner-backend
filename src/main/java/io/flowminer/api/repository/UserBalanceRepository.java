package io.flowminer.api.repository;

import io.flowminer.api.model.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, String> {
    Optional<UserBalance> findByUserId(String id);
}
