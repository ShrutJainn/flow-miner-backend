package io.flowminer.api.service;

import io.flowminer.api.model.UserBalance;
import io.flowminer.api.repository.UserBalanceRepository;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class UserBalanceService {
    private final UserBalanceRepository userBalanceRepository;

    UserBalanceService(UserBalanceRepository userBalanceRepository) {
        this.userBalanceRepository = userBalanceRepository;
    }
    public int getUserBalance(String userId) {
        UserBalance userBalance = userBalanceRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User balance not found"));

        return userBalance.getCredits();
    }
    public void setupUserBalance(String userId) {
        Optional<UserBalance> userBalance = userBalanceRepository.findByUserId(userId);

        if(userBalance.isEmpty()) {
            UserBalance newUserBalance = new UserBalance();
            newUserBalance.setUserId(userId);
            newUserBalance.setCredits(100);
            userBalanceRepository.save(newUserBalance);
        }
    }
}
