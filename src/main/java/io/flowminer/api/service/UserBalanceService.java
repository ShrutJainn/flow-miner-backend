package io.flowminer.api.service;

import io.flowminer.api.model.UserBalance;
import io.flowminer.api.repository.UserBalanceRepository;
import org.springframework.stereotype.Service;

@Service
public class UserBalanceService {
    private final UserBalanceRepository userBalanceRepository;

    UserBalanceService(UserBalanceRepository userBalanceRepository) {
        this.userBalanceRepository = userBalanceRepository;
    }
    public int getUserBalance(String userId) {
        UserBalance userBalance = userBalanceRepository.findByUserId(userId);

        return userBalance.getCredits();
    }
}
