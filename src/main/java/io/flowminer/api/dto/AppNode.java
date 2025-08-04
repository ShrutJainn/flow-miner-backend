package io.flowminer.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppNode {
    public String id;
    public String type;
    public Map<String, Object> inputs;
    public NodeData data;
}

