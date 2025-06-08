package io.flowminer.api.model;

import io.flowminer.api.enums.WorkflowExecutionStatus;
import io.flowminer.api.enums.WorkflowExecutionTrigger;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_execution")
@Data
public class WorkflowExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "workflow_id")
    private UUID workflowId;

    private String userId;
    @Enumerated(EnumType.STRING)
    private WorkflowExecutionTrigger trigger;
    @Enumerated(EnumType.STRING)
    private WorkflowExecutionStatus status;

    @Column(name = "credits_consumed")
    private int creditsConsumed = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
