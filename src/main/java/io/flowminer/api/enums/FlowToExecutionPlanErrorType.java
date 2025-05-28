package io.flowminer.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FlowToExecutionPlanErrorType {
    NO_ENTRY_POINT,
    INVALID_INPUTS
}
