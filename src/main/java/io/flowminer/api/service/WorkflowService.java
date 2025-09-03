package io.flowminer.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowminer.api.dto.AppNode;
import io.flowminer.api.dto.Edge;
import io.flowminer.api.dto.FlowDefinitionDTO;
import io.flowminer.api.dto.FlowToExecutionPlanResponse;
import io.flowminer.api.enums.WorkflowEnum;
import io.flowminer.api.exception.CustomException;
import io.flowminer.api.model.Workflow;
import io.flowminer.api.repository.WorkflowRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final FlowToExecutionPlanService flowToExecutionPlanService;
    private final ObjectMapper objectMapper;
    public WorkflowService(WorkflowRepository workflowRepository, ObjectMapper objectMapper, FlowToExecutionPlanService flowToExecutionPlanService) {
        this.workflowRepository = workflowRepository;
        this.flowToExecutionPlanService = flowToExecutionPlanService;
        this.objectMapper = objectMapper;
    }

    public List<Workflow> getWorkflowsByUser(String userId) {
        return workflowRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    public String publishWorkflow(String workflowId, String userId, String flowDefinition) throws JsonProcessingException {
        Workflow workflow = workflowRepository.findByIdAndUserId(UUID.fromString(workflowId), userId).orElseThrow(() -> {
            throw new RuntimeException("Workflow not found or doesn't belong to the specified user");
        });
        if (flowDefinition.isEmpty()) throw new RuntimeException("Flow definition is not defined");
        ObjectMapper mapper = new ObjectMapper();
        FlowDefinitionDTO flowDefinitionObject = mapper.readValue(flowDefinition, FlowDefinitionDTO.class);
        List<AppNode> nodes = flowDefinitionObject.getNodes();
        List<Edge> edges = flowDefinitionObject.getEdges();

        FlowToExecutionPlanResponse response = flowToExecutionPlanService.generatePlan(nodes, edges);
        workflow.setDefinition(flowDefinition);
        workflow.setExecutionPlan(objectMapper.writeValueAsString(response.getExecutionPlan()));
        workflow.setCreditsCost(response.getTotalCreditsConsumed());
        workflow.setStatus(WorkflowEnum.PUBLISHED);
        workflowRepository.save(workflow);
        return workflow.getId().toString();
    }
    public String unpublishWorkflow(String workflowId, String userId) {
        Workflow workflow = workflowRepository.findByIdAndUserId(UUID.fromString(workflowId), userId).orElseThrow(() -> {
            throw new RuntimeException("Workflow not found or does not belongs to the user");
        });

        if(!workflow.getStatus().equals(WorkflowEnum.PUBLISHED))
            throw new CustomException("Workflow is not published");

        workflow.setStatus(WorkflowEnum.DRAFT);
        workflow.setExecutionPlan(null);
        workflow.setCreditsCost(0);
        workflowRepository.save(workflow);
        return workflow.getId().toString();
    }
}
