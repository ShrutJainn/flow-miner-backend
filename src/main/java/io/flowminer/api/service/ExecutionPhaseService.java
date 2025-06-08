package io.flowminer.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowminer.api.dto.AppNode;
import io.flowminer.api.dto.ExecutePhaseResponseDTO;
import io.flowminer.api.enums.ExecutionPhaseStatus;
import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.registry.TaskRegistry;
import io.flowminer.api.repository.ExecutionPhaseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExecutionPhaseService {
    public final ExecutionPhaseRepository executionPhaseRepository;
    ExecutionPhaseService(ExecutionPhaseRepository executionPhaseRepository) {
        this.executionPhaseRepository = executionPhaseRepository;
    }

    private void finalizePhase(ExecutionPhase phase, boolean success) {
        ExecutionPhaseStatus finalStatus = success ? ExecutionPhaseStatus.COMPLETED : ExecutionPhaseStatus.FAILED;

        phase.setStatus(finalStatus);
        phase.setCompletedAt(LocalDateTime.now());
        executionPhaseRepository.save(phase);
    }
    private boolean executePhase(ExecutionPhase phase, AppNode node) {
        return true;
    }
    public ExecutePhaseResponseDTO executeWorkflowPhase(ExecutionPhase phase) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        AppNode node = objectMapper.readValue(phase.getNode(), AppNode.class);

        //update phase status
        phase.setStatus(ExecutionPhaseStatus.RUNNING);
        phase.setStartedAt(LocalDateTime.now());
        executionPhaseRepository.save(phase);

        int creditsConsumed = TaskRegistry.get(node.getData().getType()).getCredits();

        System.out.println("Executing phase " + phase.getName() + " with " + creditsConsumed + " credits required");

        //TODO : Decrement user balance with required credits
        boolean success = executePhase(phase, node);
        finalizePhase(phase, success);
        return new ExecutePhaseResponseDTO(success);
    }


}




