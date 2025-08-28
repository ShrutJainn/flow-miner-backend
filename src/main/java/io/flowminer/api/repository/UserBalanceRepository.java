package io.flowminer.api.repository;

import io.flowminer.api.model.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, String> {
    UserBalance findByUserId(String id);
}
