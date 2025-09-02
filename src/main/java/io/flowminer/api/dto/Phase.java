package io.flowminer.api.dto;

import io.flowminer.api.enums.EnvironmentPhaseType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Phase {
    @Enumerated(EnumType.STRING)
    private EnvironmentPhaseType type;

    private String executionPhaseId;
    Map<String, String> inputs = new HashMap<>();
    private String outputs;
    private String error;
}

