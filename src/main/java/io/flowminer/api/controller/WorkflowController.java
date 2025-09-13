package io.flowminer.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowminer.api.dto.*;
import io.flowminer.api.enums.WorkflowEnum;
import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.model.Workflow;
import io.flowminer.api.model.WorkflowExecution;
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

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    public final WorkflowService workflowService;
    public final WorkflowRepository workflowRepository;
    public final WorkflowExecutionRepository workflowExecutionRepository;
    public final FlowToExecutionPlanService flowToExecutionPlanService;
    public final ExecutionPhaseRepository executionPhaseRepository;
    public final ObjectMapper objectMapper;
    public final RestTemplate restTemplate;
    public final RedisService redisService;

    public WorkflowController(WorkflowService workflowService, RedisService redisService, RestTemplate restTemplate, WorkflowRepository workflowRepository, WorkflowExecutionRepository workflowExecutionRepository, FlowToExecutionPlanService flowToExecutionPlanService, ExecutionPhaseRepository executionPhaseRepository, ObjectMapper objectMapper) {
        this.workflowService = workflowService;
        this.workflowRepository = workflowRepository;
        this.flowToExecutionPlanService = flowToExecutionPlanService;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.executionPhaseRepository = executionPhaseRepository;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
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
    @PostMapping("/duplicate")
    public void duplicateWorkflow(@RequestBody DuplicateWorkflowRequestDTO request) {
        Workflow workflow = workflowRepository.findByIdAndUserId(UUID.fromString(request.getWorkflowId()), request.getUserId()).orElseThrow(() -> new RuntimeException("Workflow not found or does not belong to the user"));
        Workflow newWorkflow = new Workflow();
        newWorkflow.setName(request.getName());
        newWorkflow.setDescription(request.getDescription());
        newWorkflow.setUserId(request.getUserId());
        newWorkflow.setStatus(WorkflowEnum.DRAFT);
        newWorkflow.setDefinition(workflow.getDefinition());
        newWorkflow.setCreatedAt(LocalDateTime.now());
        newWorkflow.setUpdatedAt(LocalDateTime.now());
        workflowRepository.save(newWorkflow);
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

    @PostMapping("/publish")
    public ResponseEntity<String> publishWorkflow(@RequestBody GenerateWorkflowRequestDTO request) throws JsonProcessingException {
        String workflowPublishId = workflowService.publishWorkflow(request.getWorkflowId(), request.getUserId(), request.getFlowDefinition());
        return ResponseEntity.ok(workflowPublishId);
    }
    @PostMapping("/unpublish")
    public ResponseEntity<String> unpublishWorkflow(@RequestBody GenerateWorkflowRequestDTO request) {
        String workflowId = workflowService.unpublishWorkflow(request.getWorkflowId(), request.getUserId());

        return ResponseEntity.ok(workflowId);
    }
    @PostMapping("/update-cron")
    public ResponseEntity<String> updateCronWorkflow(@RequestBody UpdateCronRequestBodyDTO request) {
        String workflowId = workflowService.updateWorkflowCron(request.getWorkflowId(), request.getUserId(), request.getCron());
        return ResponseEntity.ok(workflowId);
    }
    @PostMapping("/remove-schedule")
    public void removeSchedule(@RequestBody UpdateCronRequestBodyDTO request) {
        Workflow workflow = workflowRepository.findByIdAndUserId(UUID.fromString(request.getWorkflowId()), request.getUserId()).orElseThrow(() -> new RuntimeException("Workflow does not exist of doesn't belong to the user"));

        workflow.setCron(null);
        workflow.setNextRunAt(null);
        workflowRepository.save(workflow);
    }
    @GetMapping("/periods/{userId}")
    public ResponseEntity<List<PeriodDTO>> getPeriodsForUser(@PathVariable String userId) {
        List<PeriodDTO> periods = workflowService.getPeriodsForUser(userId);
        return ResponseEntity.ok(periods);
    }

    @Data
    @AllArgsConstructor
    public static class WorkflowExecutionWithPhasesDTO {
        private WorkflowExecution workflowExecution;
        private List<ExecutionPhase> phases;
    }
    @Data
    @AllArgsConstructor
    public static class UpdateCronRequestBodyDTO {
        private String workflowId;
        private String userId;
        private String cron;
    }
}
