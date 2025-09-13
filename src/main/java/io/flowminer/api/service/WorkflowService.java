package io.flowminer.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowminer.api.dto.*;
import io.flowminer.api.enums.WorkflowEnum;
import io.flowminer.api.exception.CustomException;
import io.flowminer.api.model.Workflow;
import io.flowminer.api.repository.WorkflowExecutionRepository;
import io.flowminer.api.repository.WorkflowRepository;
import io.flowminer.api.utils.CronUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final FlowToExecutionPlanService flowToExecutionPlanService;
    private final ObjectMapper objectMapper;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    public WorkflowService(WorkflowRepository workflowRepository, ObjectMapper objectMapper, FlowToExecutionPlanService flowToExecutionPlanService, WorkflowExecutionRepository workflowExecutionRepository) {
        this.workflowRepository = workflowRepository;
        this.flowToExecutionPlanService = flowToExecutionPlanService;
        this.objectMapper = objectMapper;
        this.workflowExecutionRepository = workflowExecutionRepository;
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
    public String updateWorkflowCron(String workflowId, String userId, String cron) {
        Workflow workflow = workflowRepository.findByIdAndUserId(UUID.fromString(workflowId), userId).orElseThrow(() -> new RuntimeException("Workflow not found"));
        try {
            workflow.setCron(cron);
            LocalDateTime nextRun = CronUtils.getNextExecutionTime(cron);
            workflow.setNextRunAt(nextRun);
            workflow.setUpdatedAt(LocalDateTime.now());
            workflowRepository.save(workflow);

        } catch (IllegalArgumentException e) {  // cron-utils throws this if invalid
            throw new CustomException("Invalid cron expression: " + cron);
        } catch (RuntimeException e) {  // catch anything else from ExecutionTime calculation
            throw new CustomException("Could not calculate next execution for cron: " + cron);
        }
        return workflow.getId().toString();
    }

    public List<PeriodDTO> getPeriodsForUser(String userId) {
        LocalDateTime minStartedAt = workflowExecutionRepository.findMinStartedAtByUserId(userId);
        int currentYear = LocalDate.now().getYear();
        int minYear = (minStartedAt != null) ? minStartedAt.getYear() : currentYear;

        List<PeriodDTO> periods = new ArrayList<>();
        for(int year=minYear; year<=currentYear; year++) {
            for(int month=0; month<=11; month++) {
                periods.add(new PeriodDTO(month, year));
            }
        }
        return periods;
    }
}
