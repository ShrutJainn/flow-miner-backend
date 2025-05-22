package io.flowminer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateWorkflowDTO {
    public String userId;
    public String definition;
}
