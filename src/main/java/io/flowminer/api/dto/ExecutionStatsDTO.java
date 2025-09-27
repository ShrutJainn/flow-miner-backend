package io.flowminer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExecutionStatsDTO {
    private int workflowExecutions;
    private int creditsConsumed;
    private int phaseExecutions;
}
