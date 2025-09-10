package io.flowminer.api.service;

import io.flowminer.api.exception.CustomException;
import io.flowminer.api.model.Credentials;
import io.flowminer.api.repository.CredentialsRepository;
import io.flowminer.api.utils.EncryptionUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CredentialsService {
    private final CredentialsRepository credentialsRepository;
    CredentialsService(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }
    public List<Credentials> getCredentialsByUserId(String userId) {
        if(userId.isEmpty()) throw new RuntimeException("User id not found");

        return credentialsRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public void createCredential(String userId, String name, String value) {
        credentialsRepository.findByUserIdAndName(userId, name)
                .ifPresent(existing -> {
                    throw new CustomException("Credential with name '" + name + "' already exists for this user.");
                });
        Credentials credentials = new Credentials();
        credentials.setUserId(userId);
        credentials.setName(name);

        String encryptedValue = EncryptionUtil.encrypt(value);
        System.out.println("Encrypted value : " + encryptedValue);
        credentials.setValue(encryptedValue);
        credentials.setCreatedAt(LocalDateTime.now());
        credentialsRepository.save(credentials);
    }
}
