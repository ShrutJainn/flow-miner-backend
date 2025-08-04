package io.flowminer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface ExecutionEnvironment {
    String getInput(String name);
}
