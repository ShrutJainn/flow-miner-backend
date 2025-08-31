package io.flowminer.api.dto;

import io.flowminer.api.enums.FlowToExecutionPlanErrorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowToExecutionPlanResponse {
    public WorkflowExecutionPlan executionPlan;
    public ErrorDetail error;
    public int totalCreditsConsumed;
    @Data
    @AllArgsConstructor
    public static class ErrorDetail {
        public FlowToExecutionPlanErrorType type;
        public List<MissingInput> invalidElements;
    }
}
