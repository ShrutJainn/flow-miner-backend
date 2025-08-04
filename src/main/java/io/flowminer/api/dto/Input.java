package io.flowminer.api.dto;

import io.flowminer.api.enums.TaskParamType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Input {
    public String name;
    @Enumerated(EnumType.STRING)
    public TaskParamType type;

    public boolean required;
}
