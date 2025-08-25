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
    public final WorkflowExecutionRepository workflowExecutionRepository;
    public final WorkflowRepository workflowRepository;
    public final ExecutionPhaseRepository executionPhaseRepository;
    public final WorkflowExecutionService workflowExecutionService;
    WorkflowExecutionController(WorkflowExecutionRepository workflowExecutionRepository, WorkflowRepository workflowRepository, ExecutionPhaseRepository executionPhaseRepository, WorkflowExecutionService workflowExecutionService) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowRepository = workflowRepository;
        this.executionPhaseRepository = executionPhaseRepository;
        this.workflowExecutionService = workflowExecutionService;
    }
    @PostMapping("/execute")
    public ResponseEntity<String> executeWorkflow(@RequestBody GenerateWorkflowRequestDTO req) throws JsonProcessingException {
//        WorkflowExecution execution = workflowExecutionRepository.findById(UUID.fromString(executionId)).orElseThrow(() -> new RuntimeException("Workflow not found"));
//        Workflow workflow = workflowRepository.findById(execution.getWorkflowId()).orElseThrow(() -> new RuntimeException("Workflow not found"));
//
//        // Initialize the workflow
//        execution.setStartedAt(LocalDateTime.now());
//        execution.setStatus(WorkflowExecutionStatus.RUNNING);
//        workflowExecutionRepository.save(execution);
//
//        workflow.setLastRunAt(LocalDateTime.now());
//        workflow.setLastRunStatus(WorkflowExecutionStatus.RUNNING);
//        workflow.setLastRunId(executionId);
//        workflowRepository.save(workflow);
//
//        //initialize phase status
//        List<ExecutionPhase> executionPhases = executionPhaseRepository.findAllByWorkflowExecutionId(UUID.fromString(executionId));
//        for(ExecutionPhase phase : executionPhases) {
//            phase.setStatus(ExecutionPhaseStatus.PENDING);
//        }
//        executionPhaseRepository.saveAll(executionPhases);
//
//
//        int creditsConsumed = 0;
//        boolean executionFailed = false;
//
//        //TODO : execute the phase and consume credits
//
//        //finalize workflow
//        WorkflowExecutionStatus finalStatus = executionFailed ? WorkflowExecutionStatus.FAILED : WorkflowExecutionStatus.COMPLETED;
//        execution.setStatus(finalStatus);
//        execution.setCompletedAt(LocalDateTime.now());
//        execution.setCreditsConsumed(creditsConsumed);
//        workflowExecutionRepository.save(execution);
//
//        Workflow workflowAfterExecution = workflowRepository.findByIdAndLastRunId(execution.getWorkflowId(), executionId);
//        workflowAfterExecution.setLastRunStatus(finalStatus);
//        workflowRepository.save(workflowAfterExecution);


        String workflowExecutionId = workflowExecutionService.executeWorkflow(req.getWorkflowId(), req.getUserId(), req.getFlowDefinition());
        return ResponseEntity.ok(workflowExecutionId);
    }
}
