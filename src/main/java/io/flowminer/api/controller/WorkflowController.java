package io.flowminer.api.controller;

import io.flowminer.api.dto.CreateWorkflowRequestDTO;
import io.flowminer.api.dto.DeleteWorkflowDTO;
import io.flowminer.api.enums.WorkflowEnum;
import io.flowminer.api.model.Workflow;
import io.flowminer.api.repository.WorkflowRepository;
import io.flowminer.api.service.WorkflowService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    public final WorkflowService workflowService;
    public final WorkflowRepository workflowRepository;
    public WorkflowController(WorkflowService workflowService, WorkflowRepository workflowRepository) {
        this.workflowService = workflowService;
        this.workflowRepository = workflowRepository;
    }

    @GetMapping
    public List<Workflow> getWorkflowsForUser(@RequestParam String userId) {
        return workflowService.getWorkflowsByUser(userId);
    }
    @PostMapping("/create")
    public Workflow createWorkflow(@RequestBody CreateWorkflowRequestDTO req) {
        Workflow workflow = new Workflow();
        workflow.setUserId(req.userId);
        workflow.setName(req.name);
        workflow.setDefinition(req.description);
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

        if(workflow.isEmpty()) throw new RuntimeException("Workflow not found or doesn't belong to the user");

        workflowRepository.delete(workflow.get());
    }
}
