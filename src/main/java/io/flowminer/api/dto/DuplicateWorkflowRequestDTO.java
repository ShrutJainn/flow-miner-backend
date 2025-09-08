package io.flowminer.api.dto;

import io.flowminer.api.enums.WorkflowEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DuplicateWorkflowRequestDTO {
    public String workflowId;
    public String userId;
    public String name;
    public String description;
}
