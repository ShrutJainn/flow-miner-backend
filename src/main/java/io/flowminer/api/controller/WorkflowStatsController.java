package io.flowminer.api.controller;

import io.flowminer.api.dto.DailyStatDTO;
import io.flowminer.api.dto.DateRangeDTO;
import io.flowminer.api.dto.ExecutionStatsDTO;
import io.flowminer.api.model.WorkflowExecution;
import io.flowminer.api.service.WorkflowStatsService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class WorkflowStatsController {
    private final WorkflowStatsService workflowStatsService;
    public WorkflowStatsController(WorkflowStatsService workflowStatsService) {
        this.workflowStatsService = workflowStatsService;
    }

    @PostMapping
    public ResponseEntity<ExecutionStatsDTO> getStats(@RequestParam String userId, @RequestBody DateRangeDTO dateRange) {
        if(userId.isEmpty()) throw new RuntimeException("Unauthorized");

        ExecutionStatsDTO stats = workflowStatsService.calculateTotalStats(userId, dateRange);
        return ResponseEntity.ok(stats);
    }
    @PostMapping("/executions")
    public ResponseEntity<List<DailyStatResponseDTO>> getExecutions(
            @RequestParam String userId,
            @RequestBody DateRangeDTO dateRange
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<DailyStatResponseDTO> executions = workflowStatsService.calculateStats(userId, dateRange);


        return ResponseEntity.ok(executions);
    }

    @Data
    @AllArgsConstructor
    public static class DailyStatResponseDTO {
        private String date;
        private int success;
        private int failed;
    }
}
