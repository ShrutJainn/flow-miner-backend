package io.flowminer.api.controller;

import io.flowminer.api.service.UserBalanceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/userBalance")
public class UserBalanceController {
    private final UserBalanceService userBalanceService;

    UserBalanceController(UserBalanceService userBalanceService) {
        this.userBalanceService = userBalanceService;
    }

    @GetMapping("/{userId}")
    public int getUserBalance(@PathVariable String userId) {
        return userBalanceService.getUserBalance(userId);
    }
}
