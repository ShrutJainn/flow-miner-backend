package io.flowminer.api.service;


import io.flowminer.api.dto.*;
import io.flowminer.api.enums.FlowToExecutionPlanErrorType;
import io.flowminer.api.registry.TaskDefinition;
import io.flowminer.api.registry.TaskRegistry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlowToExecutionPlanService {
    public FlowToExecutionPlanResponse generatePlan(List<AppNode> nodes, List<Edge> edges) {
        Map<String, AppNode> nodeMap = nodes.stream()
                .collect(Collectors.toMap(AppNode::getId, node -> node));
        int totalCreditsConsumed = 0;


        // 1. Find entry point
        List<AppNode> entryPoints = nodes.stream()
                .filter(node -> Optional.ofNullable(TaskRegistry.get(node.getData().getType().toString()))
                        .map(TaskDefinition::isEntryPoint)
                        .orElse(false))
                .toList();

        if (entryPoints.isEmpty()) {
            FlowToExecutionPlanResponse.ErrorDetail error = new FlowToExecutionPlanResponse.ErrorDetail(
                    FlowToExecutionPlanErrorType.NO_ENTRY_POINT, new ArrayList<>());
            return new FlowToExecutionPlanResponse(null, error, 0);
        }

        Set<String> planned = new HashSet<>();
        List<WorkflowExecutionPlanPhase> phases = new ArrayList<>();

        while (planned.size() < nodes.size()) {
            List<AppNode> executableNodes = nodes.stream()
                    .filter(node -> !planned.contains(node.getId()))
                    .filter(node -> getInvalidInputs(node, edges, planned).isEmpty())
                    .collect(Collectors.toList());

            if (executableNodes.isEmpty()) {
                List<MissingInput> missingInputs = nodes.stream()
                        .filter(node -> !planned.contains(node.getId()))
                        .map(node -> new MissingInput(
                                node.getId(),
                                getInvalidInputs(node, edges, planned)
                        ))
                        .collect(Collectors.toList());

                FlowToExecutionPlanResponse.ErrorDetail error = new FlowToExecutionPlanResponse.ErrorDetail(
                        FlowToExecutionPlanErrorType.INVALID_INPUTS, missingInputs);
                return new FlowToExecutionPlanResponse(null, error, 0);
            }

            for (AppNode node : executableNodes) {
                List<Edge> incomingEdges = edges.stream()
                        .filter(edge -> edge.getTarget().equals(node.getId()))
                        .filter(edge -> planned.contains(edge.getSource()))
                        .toList();

                TaskDefinition task = TaskRegistry.get(node.getData().getType().toString());
                int credits = TaskRegistry.get(node.getData().getType().toString()).getCredits();
                totalCreditsConsumed += credits;
                if (task != null && task.getInputs() != null) {
                    for (Input input : task.getInputs()) {
                        String inputName = input.getName();

                        // If input is already set, skip
                        if (node.getData().getInputs() != null &&
                                node.getData().getInputs().containsKey(inputName) &&
                                !node.getData().getInputs().get(inputName).isBlank()) {
                            continue;
                        }

                        // Find matching edge for the input
                        Optional<Edge> matchingEdge = incomingEdges.stream()
                                .filter(edge -> edge.getTargetHandle().equals(inputName))
                                .findFirst();

                        matchingEdge.ifPresent(edge -> {
                            node.getData().getInputs().put(inputName, "$ref:" + edge.getSource());
                        });
                    }
                }
                planned.add(node.getId());
            }

            phases.add(new WorkflowExecutionPlanPhase(phases.size(), executableNodes));
        }

        System.out.println("Execution plan from backend : " + phases);
        return new FlowToExecutionPlanResponse(new WorkflowExecutionPlan(phases), null, totalCreditsConsumed);
    }

    private List<String> getInvalidInputs(AppNode node, List<Edge> edges, Set<String> planned) {
        TaskDefinition task = TaskRegistry.get(node.getData().getType().toString());
        if (task == null) return List.of();

        Set<String> incomingHandles = edges.stream()
                .filter(edge -> edge.getTarget().equals(node.getId()))
                .filter(edge -> planned.contains(edge.getSource()))
                .map(Edge::getTargetHandle)
                .collect(Collectors.toSet());

        return task.getInputs().stream()
                .filter(Input::isRequired)
                .map(Input::getName)
                .filter(inputName -> !incomingHandles.contains(inputName) &&
                        (node.getData().getInputs() == null ||
                                !node.getData().getInputs().containsKey(inputName) ||
                                node.getData().getInputs().get(inputName).isBlank()))
                .collect(Collectors.toList());
    }
}
