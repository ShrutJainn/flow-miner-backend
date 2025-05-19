package io.flowminer.api.dto;

import io.flowminer.api.enums.WorkflowEnum;
import lombok.Data;

@Data
public class CreateWorkflowRequestDTO {
    public String userId;
    public String name;
    public String description;
    public String definition;
    public WorkflowEnum status;
}


