package io.flowminer.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.flowminer.api.dto.ExecutePhaseResponseDTO;
import io.flowminer.api.enums.ExecutionPhaseStatus;
import io.flowminer.api.enums.WorkflowExecutionStatus;
import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.model.Workflow;
import io.flowminer.api.model.WorkflowExecution;
import io.flowminer.api.repository.ExecutionPhaseRepository;
import io.flowminer.api.repository.WorkflowExecutionRepository;
import io.flowminer.api.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WorkflowExecutionService {
    public final WorkflowExecutionRepository workflowExecutionRepository;
    public final WorkflowRepository workflowRepository;
    public final ExecutionPhaseRepository executionPhaseRepository;
    public final ExecutionPhaseService executionPhaseService;
    WorkflowExecutionService(WorkflowExecutionRepository workflowExecutionRepository, WorkflowRepository workflowRepository, ExecutionPhaseRepository executionPhaseRepository, ExecutionPhaseService executionPhaseService) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowRepository = workflowRepository;
        this.executionPhaseRepository = executionPhaseRepository;
        this.executionPhaseService = executionPhaseService;
    }

    public void executeWorkflow(UUID executionId) throws JsonProcessingException {
        WorkflowExecution execution = initializeWorkflow(executionId);
        List<ExecutionPhase> phases = executionPhaseRepository.findAllByWorkflowExecutionId(executionId);
        initializePhases(executionId);

        int creditsConsumed = 0;
        boolean executionFailed = false;

        //TODO : Execute the phases and consume credits
        for(ExecutionPhase phase : phases) {
            ExecutePhaseResponseDTO response = executionPhaseService.executeWorkflowPhase(phase);
            if(!response.isSuccess()) {
                executionFailed = true;
                break;
            }
        }

        finalizeWorkflow(execution, creditsConsumed, executionFailed);
    }
    private WorkflowExecution initializeWorkflow(UUID executionId) {
        WorkflowExecution execution = workflowExecutionRepository.findById(executionId).orElseThrow(() -> new RuntimeException("Workflow not found"));
        Workflow workflow = workflowRepository.findById(execution.getWorkflowId()).orElseThrow(() -> new RuntimeException("Workflow not found"));

        execution.setStartedAt(LocalDateTime.now());
        execution.setStatus(WorkflowExecutionStatus.RUNNING);
        workflowExecutionRepository.save(execution);

        workflow.setLastRunAt(LocalDateTime.now());
        workflow.setLastRunId(executionId.toString());
        workflow.setLastRunStatus(WorkflowExecutionStatus.RUNNING);
        workflowRepository.save(workflow);
        return execution;
    }

    private void initializePhases(UUID executionId) {
        List<ExecutionPhase> executionPhases = executionPhaseRepository.findAllByWorkflowExecutionId(executionId);
        for(ExecutionPhase phase : executionPhases) {
            phase.setStatus(ExecutionPhaseStatus.PENDING);
        }
        executionPhaseRepository.saveAll(executionPhases);
    }
    private void finalizeWorkflow(WorkflowExecution execution, int creditsConsumed, boolean executionFailed) {
        WorkflowExecutionStatus finalStatus = executionFailed ? WorkflowExecutionStatus.FAILED : WorkflowExecutionStatus.COMPLETED;
        execution.setStatus(finalStatus);
        execution.setCompletedAt(LocalDateTime.now());
        execution.setCreditsConsumed(creditsConsumed);
        workflowExecutionRepository.save(execution);

        Workflow workflowAfterExecution = workflowRepository.findByIdAndLastRunId(execution.getWorkflowId(), execution.getId().toString());
        workflowAfterExecution.setLastRunStatus(finalStatus);
        workflowRepository.save(workflowAfterExecution);
    }
}
