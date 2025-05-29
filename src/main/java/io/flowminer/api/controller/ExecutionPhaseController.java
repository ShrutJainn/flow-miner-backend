package io.flowminer.api.controller;

import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.repository.ExecutionPhaseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/execution")
public class ExecutionPhaseController {
    public final ExecutionPhaseRepository executionPhaseRepository;

    ExecutionPhaseController(ExecutionPhaseRepository executionPhaseRepository) {
        this.executionPhaseRepository = executionPhaseRepository;
    }

    @GetMapping("/phaseDetails/{id}")
    public ExecutionPhase getWorkflowPhaseDetails(@PathVariable String id) {
        Optional<ExecutionPhase> phaseOpt =  executionPhaseRepository.findById(UUID.fromString(id));

        if(phaseOpt.isEmpty()) throw new RuntimeException("Phase not found");
        return phaseOpt.get();
    }
}
