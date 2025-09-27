package io.flowminer.api.service;

import io.flowminer.api.controller.WorkflowStatsController;
import io.flowminer.api.dto.DailyStatDTO;
import io.flowminer.api.dto.DateRangeDTO;
import io.flowminer.api.dto.ExecutionStatsDTO;
import io.flowminer.api.enums.WorkflowExecutionStatus;
import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.model.WorkflowExecution;
import io.flowminer.api.repository.ExecutionPhaseRepository;
import io.flowminer.api.repository.WorkflowExecutionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowStatsService {
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final ExecutionPhaseRepository executionPhaseRepository;
    public WorkflowStatsService(WorkflowExecutionRepository workflowExecutionRepository, ExecutionPhaseRepository executionPhaseRepository) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.executionPhaseRepository = executionPhaseRepository;
    }
    public ExecutionStatsDTO calculateTotalStats(String userId, DateRangeDTO dateRange) {
        List<WorkflowExecutionStatus> statuses = Arrays.asList(
                WorkflowExecutionStatus.COMPLETED,
                WorkflowExecutionStatus.FAILED
        );
        List<WorkflowExecution> executions = workflowExecutionRepository.findByUserIdAndDateRangeAndStatuses(userId, dateRange.getStartDate(), dateRange.getEndDate(), statuses);

        int workflowExecutions = executions.size();
        int creditsConsumed = executions.stream().mapToInt(WorkflowExecution::getCreditsConsumed).sum();
        List<UUID> executionIds = executions.stream().map(WorkflowExecution::getId).toList();

        int phaseExecutions = 0;
        if(!executionIds.isEmpty()) {
            List<ExecutionPhase> phases = executionPhaseRepository.findByExecutionIdsWithCredits(executionIds);
            phaseExecutions = phases.size();
        }
        return new ExecutionStatsDTO(workflowExecutions, creditsConsumed, phaseExecutions);
    }

    public List<WorkflowStatsController.DailyStatResponseDTO> calculateStats(String userId, DateRangeDTO dateRange) {
        List<WorkflowExecution> executions = workflowExecutionRepository.findAllByUserIdAndDateRange(userId, dateRange.getStartDate(), dateRange.getEndDate());
        Map<String, DailyStatDTO> stats = new LinkedHashMap<>();
        LocalDate start = dateRange.getStartDate().toLocalDate();
        LocalDate end = dateRange.getEndDate().toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate curr = start;
        while(!curr.isAfter(end)) {
            stats.put(curr.format(formatter), new DailyStatDTO());
            curr = curr.plusDays(1);
        }

        for(WorkflowExecution execution : executions) {
            String dateKey = execution.getStartedAt().toLocalDate().format(formatter);
            DailyStatDTO dailyStat = stats.get(dateKey);
            if(dailyStat == null) continue;

            if(execution.getStatus().equals(WorkflowExecutionStatus.COMPLETED)) dailyStat.incrementSuccess();

            else if(execution.getStatus().equals(WorkflowExecutionStatus.FAILED)) dailyStat.incrementFailed();
        }

        return stats.entrySet().stream()
                .map(entry -> new WorkflowStatsController.DailyStatResponseDTO(
                        entry.getKey(),
                        entry.getValue().getSuccess(),
                        entry.getValue().getFailed()
                ))
                .collect(Collectors.toList());
    }

}
