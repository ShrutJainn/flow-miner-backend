package io.flowminer.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Edge {
    public String source;
    public String sourceHandle;
    public String target;
    public String targetHandle;
}
