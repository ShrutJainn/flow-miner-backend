package io.flowminer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowDefinitionDTO {
    private List<AppNode> nodes;
    private List<Edge> edges;
    private Map<String, Object> viewport;
}
