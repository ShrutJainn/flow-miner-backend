package io.flowminer.api.controller;

import io.flowminer.api.dto.CreateCredentialRequestDTO;
import io.flowminer.api.exception.CustomException;
import io.flowminer.api.model.Credentials;
import io.flowminer.api.repository.CredentialsRepository;
import io.flowminer.api.service.CredentialsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credentials")
public class CredentialsController {
    private final CredentialsService credentialsService;
    private final CredentialsRepository credentialsRepository;
    CredentialsController(CredentialsService credentialsService, CredentialsRepository credentialsRepository) {
        this.credentialsService = credentialsService;
        this.credentialsRepository = credentialsRepository;
    }
    @GetMapping("/{userId}")
    public List<Credentials> getCredentialsByUser(@PathVariable String userId) {

        return credentialsService.getCredentialsByUserId(userId);
    }

    @PostMapping("/create")
    public void createCredential(@RequestBody CreateCredentialRequestDTO request) {
        if(request.getUserId().isEmpty()) throw new CustomException("User not found");

        if(request.getName().isEmpty() || request.getValue().isEmpty()) throw new CustomException("Name or Value can't be null");

        credentialsService.createCredential(request.getUserId(), request.getName(), request.getValue());
    }
    @DeleteMapping("/delete")
    public void deleteCredential(@RequestParam String userId, @RequestParam String name) {
        credentialsService.deleteCredential(userId, name);
    }
}
