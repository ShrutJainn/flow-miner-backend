package io.flowminer.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeleteWorkflowDTO {
    public String id;
    public String userId;
}
