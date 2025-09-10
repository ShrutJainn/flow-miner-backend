package io.flowminer.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Credentials {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String userId;
    private String name;
    private String value;
    private LocalDateTime createdAt = LocalDateTime.now();
}
