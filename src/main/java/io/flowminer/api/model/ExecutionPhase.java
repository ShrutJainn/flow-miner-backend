package io.flowminer.api.model;

import io.flowminer.api.enums.ExecutionPhaseStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "execution_phase")
@Data
public class ExecutionPhase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String userId;
    @Enumerated(EnumType.ORDINAL)
    private ExecutionPhaseStatus status; //status of each individual task
    private int number; //number of phases

    @Column(columnDefinition = "TEXT")
    private String node;

    private String name;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private String inputs;
    private String outputs;

    private int creditsCost;

    private UUID workflowExecutionId;
}
