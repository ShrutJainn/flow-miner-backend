package io.flowminer.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.flowminer.api.dto.GenerateWorkflowRequestDTO;
import io.flowminer.api.enums.ExecutionPhaseStatus;
import io.flowminer.api.enums.WorkflowExecutionStatus;
import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.model.Workflow;
import io.flowminer.api.model.WorkflowExecution;
import io.flowminer.api.repository.ExecutionPhaseRepository;
import io.flowminer.api.repository.WorkflowExecutionRepository;
import io.flowminer.api.repository.WorkflowRepository;
import io.flowminer.api.service.WorkflowExecutionService;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow-execution")
public class WorkflowExecutionController {
    public final WorkflowExecutionService workflowExecutionService;
    public final WorkflowExecutionRepository workflowExecutionRepository;
    WorkflowExecutionController(WorkflowExecutionService workflowExecutionService, WorkflowExecutionRepository workflowExecutionRepository) {
        this.workflowExecutionService = workflowExecutionService;
        this.workflowExecutionRepository = workflowExecutionRepository;
    }
    @PostMapping("/execute")
    public ResponseEntity<String> executeWorkflow(@RequestBody GenerateWorkflowRequestDTO req) throws JsonProcessingException {

        String workflowExecutionId = workflowExecutionService.executeWorkflow(req.getWorkflowId(), req.getUserId(), req.getFlowDefinition());
        return ResponseEntity.ok(workflowExecutionId);
    }
    @GetMapping("/")
    public List<WorkflowExecution> getWorkflowExecutions(@RequestParam String workflowId, @RequestParam String userId) {
        if(userId.isEmpty() || workflowId.isEmpty()) throw new IllegalArgumentException("Invalid arguments");

        return workflowExecutionRepository.findByWorkflowIdAndUserIdOrderByCreatedAtDesc(UUID.fromString(workflowId), userId);
    }
}
