package io.flowminer.api.model;

import io.flowminer.api.enums.WorkflowEnum;
import jakarta.persistence.*;
import lombok.Data;
import org.w3c.dom.Text;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "name"}))
@Data
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "userId", columnDefinition = "VARCHAR(255)")
    private String userId;
    private String name;
    private String description;
    @Column(columnDefinition = "TEXT")
    private String definition;

    @Enumerated(EnumType.STRING)
    private WorkflowEnum status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
