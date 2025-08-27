package io.flowminer.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowminer.api.dto.*;
import io.flowminer.api.enums.ExecutionPhaseStatus;
import io.flowminer.api.enums.WorkflowEnum;
import io.flowminer.api.enums.WorkflowExecutionStatus;
import io.flowminer.api.enums.WorkflowExecutionTrigger;
import io.flowminer.api.model.EnvironmentModel;
import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.model.Workflow;
import io.flowminer.api.model.WorkflowExecution;
import io.flowminer.api.registry.TaskRegistry;
import io.flowminer.api.repository.EnvironmentRepository;
import io.flowminer.api.repository.ExecutionPhaseRepository;
import io.flowminer.api.repository.WorkflowExecutionRepository;
import io.flowminer.api.repository.WorkflowRepository;
import io.flowminer.api.service.FlowToExecutionPlanService;
import io.flowminer.api.service.RedisService;
import io.flowminer.api.service.WorkflowService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.coyote.Response;
import org.hibernate.jdbc.Work;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    public final WorkflowService workflowService;
    public final WorkflowRepository workflowRepository;
    public final WorkflowExecutionRepository workflowExecutionRepository;
    public final FlowToExecutionPlanService flowToExecutionPlanService;
    public final ExecutionPhaseRepository executionPhaseRepository;
    public final ObjectMapper objectMapper;
    public final EnvironmentRepository environmentRepository;
    public final RestTemplate restTemplate;
    public final RedisService redisService;

    public WorkflowController(WorkflowService workflowService, RedisService redisService, RestTemplate restTemplate, WorkflowRepository workflowRepository, WorkflowExecutionRepository workflowExecutionRepository, FlowToExecutionPlanService flowToExecutionPlanService, ExecutionPhaseRepository executionPhaseRepository, ObjectMapper objectMapper, EnvironmentRepository environmentRepository) {
        this.workflowService = workflowService;
        this.workflowRepository = workflowRepository;
        this.flowToExecutionPlanService = flowToExecutionPlanService;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.executionPhaseRepository = executionPhaseRepository;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.environmentRepository = environmentRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/user")
    public List<Workflow> getWorkflowsForUser(@RequestParam String userId) {
        return workflowService.getWorkflowsByUser(userId);
    }

    @PostMapping("/create")
    public Workflow createWorkflow(@RequestBody CreateWorkflowRequestDTO req) {
        Workflow workflow = new Workflow();
        workflow.setUserId(req.userId);
        workflow.setName(req.name);
        workflow.setDescription(req.description);
        workflow.setStatus(req.status);
        workflow.setDefinition(req.definition);
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());

        workflowRepository.save(workflow);
        return workflow;
    }

    @DeleteMapping("/delete")
    public void deleteWorkflow(@RequestParam String id, @RequestParam String userId) {
        Optional<Workflow> workflow = workflowRepository.findByIdAndUserId(UUID.fromString(id), userId);

        if (workflow.isEmpty()) throw new RuntimeException("Workflow not found or doesn't belong to the user");

        workflowRepository.delete(workflow.get());
    }

    @GetMapping("/{workflowId}")
    public Workflow getWorkflowById(@PathVariable String workflowId) {
        Optional<Workflow> workflow = workflowRepository.findById(UUID.fromString(workflowId));
        if (workflow.isEmpty()) throw new RuntimeException("Workflow not found");

        return workflow.get();
    }

    @PostMapping("/update/{id}")
    public Workflow updateWorkflow(@PathVariable String id, @RequestBody UpdateWorkflowDTO req) {
        Optional<Workflow> workflowOpt = workflowRepository.findByIdAndUserId(UUID.fromString(id), req.userId);

        if (workflowOpt.isEmpty()) throw new RuntimeException("Workflow not found or does not belong to the user");
        Workflow workflow = workflowOpt.get();
        if (workflow.getStatus() != WorkflowEnum.DRAFT) throw new RuntimeException("Workflow is not a draft");
        workflow.setDefinition(req.definition);
        workflowRepository.save(workflow);
        return workflow;
    }
    @GetMapping("/execution/{executionId}")
    public ResponseEntity<WorkflowExecutionWithPhasesDTO> getWorkflowExecutionWithPhases(@PathVariable String executionId) {
        WorkflowExecution workflowExecution = workflowExecutionRepository.findById(UUID.fromString(executionId)).orElseThrow(() -> new RuntimeException("Execution not found"));
        List<ExecutionPhase> phases = executionPhaseRepository.findAllByWorkflowExecutionId(UUID.fromString(executionId));

        phases.sort(Comparator.comparingInt(ExecutionPhase::getNumber));
        WorkflowExecutionWithPhasesDTO response = new WorkflowExecutionWithPhasesDTO(workflowExecution, phases);
        return ResponseEntity.ok(response);
    }

    @Data
    @AllArgsConstructor
    public static class WorkflowExecutionWithPhasesDTO {
        private WorkflowExecution workflowExecution;
        private List<ExecutionPhase> phases;
    }
}
