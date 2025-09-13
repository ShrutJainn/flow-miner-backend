package io.flowminer.api.model;

import jakarta.persistence.*;
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
    @Column(columnDefinition = "TEXT")
    private String value;
    private LocalDateTime createdAt = LocalDateTime.now();
}
