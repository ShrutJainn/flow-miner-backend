package io.flowminer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
    @AllArgsConstructor
    public class GenerateWorkflowRequestDTO {
        public String workflowId;
        public String userId;
        public String flowDefinition;
    }