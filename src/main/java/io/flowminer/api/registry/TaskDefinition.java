package io.flowminer.api.registry;

import io.flowminer.api.dto.Input;
import io.flowminer.api.dto.Output;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TaskDefinition {
    private boolean isEntryPoint;
    private List<Input> inputs;
    private List<Output> outputs;
    private int credits;
    private String label;
}
