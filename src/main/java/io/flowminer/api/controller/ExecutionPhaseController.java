package io.flowminer.api.controller;

import io.flowminer.api.dto.ScreenshotRequestDTO;
import io.flowminer.api.dto.ScreenshotResponse;
import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.repository.ExecutionPhaseRepository;
import io.flowminer.api.service.PuppeteerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/execution")
public class ExecutionPhaseController {
    public final ExecutionPhaseRepository executionPhaseRepository;
    public final PuppeteerService puppeteerService;
    ExecutionPhaseController(ExecutionPhaseRepository executionPhaseRepository, PuppeteerService puppeteerService) {
        this.executionPhaseRepository = executionPhaseRepository;
        this.puppeteerService = puppeteerService;
    }

    @GetMapping("/phaseDetails/{id}")
    public ExecutionPhase getWorkflowPhaseDetails(@PathVariable String id) {
        Optional<ExecutionPhase> phaseOpt =  executionPhaseRepository.findById(UUID.fromString(id));

        if(phaseOpt.isEmpty()) throw new RuntimeException("Phase not found");
        return phaseOpt.get();
    }

    @PostMapping("/screenshot")
    public ResponseEntity<ScreenshotResponse> getScreenshot(@RequestBody ScreenshotRequestDTO request) {
        ScreenshotResponse response = puppeteerService.takeScreenshot(request.getUrl());
        return ResponseEntity.ok(response);
    }
}
