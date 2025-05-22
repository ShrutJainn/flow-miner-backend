package io.flowminer.api.controller;

import io.flowminer.api.dto.CreateWorkflowRequestDTO;
import io.flowminer.api.dto.DeleteWorkflowDTO;
import io.flowminer.api.dto.UpdateWorkflowDTO;
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

        if(workflow.isEmpty()) throw new RuntimeException("Workflow not found or doesn't belong to the user");

        workflowRepository.delete(workflow.get());
    }

    @GetMapping("/{workflowId}")
    public Workflow getWorkflowById(@PathVariable String workflowId) {
        Optional<Workflow> workflow = workflowRepository.findById(UUID.fromString(workflowId));
        if(workflow.isEmpty()) throw new RuntimeException("Workflow not found");

        return workflow.get();
    }

    @PostMapping("/update/{id}")
    public Workflow updateWorkflow(@PathVariable String id, @RequestBody UpdateWorkflowDTO req) {
        Optional<Workflow> workflowOpt = workflowRepository.findByIdAndUserId(UUID.fromString(id), req.userId);

        if(workflowOpt.isEmpty()) throw new RuntimeException("Workflow not found or does not belong to the user");
        Workflow workflow = workflowOpt.get();
        if(workflow.getStatus() != WorkflowEnum.DRAFT) throw  new RuntimeException("Workflow is not a draft");
        workflow.setDefinition(req.definition);
        workflowRepository.save(workflow);
        return workflow;
    }
}
