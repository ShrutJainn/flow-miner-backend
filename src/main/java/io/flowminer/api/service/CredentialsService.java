package io.flowminer.api.service;

import io.flowminer.api.exception.CustomException;
import io.flowminer.api.model.Credentials;
import io.flowminer.api.repository.CredentialsRepository;
import io.flowminer.api.utils.EncryptionUtil;
import jakarta.transaction.Transactional;
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
        if(userId.isEmpty()) throw new RuntimeException("User not found");

        List<Credentials> credentials = credentialsRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

//        for(Credentials cred : credentials) {
//            cred.setValue(EncryptionUtil.decrypt(cred.getValue()));
//        }
        return credentials;
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
    @Transactional
    public void deleteCredential(String userId, String name) {
        if(userId.isEmpty() || name.isEmpty()) throw new RuntimeException("400 bad request");

        Credentials credential = credentialsRepository.findByUserIdAndName(userId, name)
                .orElseThrow(() -> new RuntimeException("The user does not have the credential"));

        credentialsRepository.deleteByUserIdAndName(userId, name);
    }
}
