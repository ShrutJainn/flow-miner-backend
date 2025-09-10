package io.flowminer.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateCredentialRequestDTO {
    public String userId;
    public String name;
    public String value;
}
