package io.flowminer.api.service;

import io.flowminer.api.model.Workflow;
import io.flowminer.api.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    public WorkflowService(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public List<Workflow> getWorkflowsByUser(String userId) {
        return workflowRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }
}
