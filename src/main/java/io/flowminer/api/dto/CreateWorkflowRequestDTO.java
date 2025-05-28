package io.flowminer.api.dto;

import io.flowminer.api.enums.WorkflowEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateWorkflowRequestDTO {
    public String userId;
    public String name;
    public String description;
    public String definition;
    public WorkflowEnum status;
}


