package io.flowminer.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowminer.api.dto.*;
import io.flowminer.api.enums.ExecutionPhaseStatus;
import io.flowminer.api.enums.WorkflowExecutionStatus;
import io.flowminer.api.enums.WorkflowExecutionTrigger;
import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.model.Workflow;
import io.flowminer.api.model.WorkflowExecution;
import io.flowminer.api.registry.TaskRegistry;
import io.flowminer.api.repository.ExecutionPhaseRepository;
import io.flowminer.api.repository.WorkflowExecutionRepository;
import io.flowminer.api.repository.WorkflowRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowExecutionService {
    public final WorkflowExecutionRepository workflowExecutionRepository;
    public final WorkflowRepository workflowRepository;
    public final ExecutionPhaseRepository executionPhaseRepository;
    public final ExecutionPhaseService executionPhaseService;
    public final RestTemplate restTemplate;
    public final FlowToExecutionPlanService flowToExecutionPlanService;
    public final ObjectMapper objectMapper;
    public final RedisService redisService;

    WorkflowExecutionService(WorkflowExecutionRepository workflowExecutionRepository, RedisService redisService, WorkflowRepository workflowRepository, ObjectMapper objectMapper, FlowToExecutionPlanService flowToExecutionPlanService, ExecutionPhaseRepository executionPhaseRepository, ExecutionPhaseService executionPhaseService, RestTemplate restTemplate) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowRepository = workflowRepository;
        this.executionPhaseRepository = executionPhaseRepository;
        this.executionPhaseService = executionPhaseService;
        this.restTemplate = restTemplate;
        this.flowToExecutionPlanService = flowToExecutionPlanService;
        this.objectMapper = objectMapper;
        this.redisService = redisService;
    }

    public String executeWorkflow(String workflowId, String userId, String flowDefinition) throws JsonProcessingException {
        WorkflowExecution execution = initializeWorkflow(workflowId, userId);
//        List<ExecutionPhase> phases = executionPhaseRepository.findAllByWorkflowExecutionId(executionId);
//        initializePhases(executionId);

        int creditsConsumed = 0;
        boolean executionFailed = false;
//        Environment environment = new Environment();
//
//        for(ExecutionPhase phase : phases) {
//            ExecutePhaseResponseDTO response = executionPhaseService.executeWorkflowPhase(phase, environment);
//            if(!response.isSuccess()) {
//                executionFailed = true;
//                break;
//            }
//        }

        // ----------------------------------------------------------------

        Optional<Workflow> workflowOpt = workflowRepository.findByIdAndUserId(UUID.fromString(workflowId), userId);
        if (workflowOpt.isEmpty())
            throw new RuntimeException("Workflow not found or doesn't belong to the specified user");
        Workflow workflow = workflowOpt.get();

        if (flowDefinition.isEmpty()) throw new RuntimeException("Flow definition is not defined");

        ObjectMapper mapper = new ObjectMapper();
        FlowDefinitionDTO flowDefinitionObject = mapper.readValue(flowDefinition, FlowDefinitionDTO.class);
        List<AppNode> nodes = flowDefinitionObject.getNodes();
        List<Edge> edges = flowDefinitionObject.getEdges();

        FlowToExecutionPlanResponse response = flowToExecutionPlanService.generatePlan(nodes, edges);

        Environment environment = new Environment();
        //TODO : Execute the phases and consume credits
        for (WorkflowExecutionPlanPhase phase : response.getExecutionPlan().getPhases()) {
            for (AppNode node : phase.getNodes()) {
                environment.getPhases().put(node.getId(), new Phase(node.getData().getType(), node.getData().getInputs(), node.getData().getOutputs()));
            }
        }

        ObjectMapper cleanMapper = objectMapper.copy();
        cleanMapper.deactivateDefaultTyping();


        String redisKey = "env:" + workflow.getId();
        redisService.saveEnvironmentOnRedis(redisKey, cleanMapper.writerWithDefaultPrettyPrinter().writeValueAsString(environment));

        String nodeUrl = System.getenv("NODE_PUP_URL") + "/execute/" + workflow.getId().toString();


        try {
            ResponseEntity<Map> environmentFromNodeServer = restTemplate.postForEntity(nodeUrl, null, Map.class);

            if (environmentFromNodeServer.getStatusCode().is2xxSuccessful()) {
                Boolean success = (Boolean) environmentFromNodeServer.getBody().get("success");

                if (Boolean.TRUE.equals(success)) {
                    String updatedEnv = redisService.getEnvironment(redisKey);

                    Map<String, Object> envMap = new ObjectMapper().readValue(updatedEnv, Map.class);
                    System.out.println("updated environment from redis : " + envMap);

                } else {
                    System.err.println("Node server error : " + environmentFromNodeServer.getBody().get("error"));
                }
            } else {
                System.err.println("Node server returned status : " + environmentFromNodeServer.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

                List<ExecutionPhase> phases = new ArrayList<>();

        for (WorkflowExecutionPlanPhase phase : response.getExecutionPlan().getPhases()) {
            for (AppNode node : phase.getNodes()) {
                ExecutionPhase executionPhase = new ExecutionPhase();
                executionPhase.setWorkflowExecutionId(execution.getId());
                executionPhase.setUserId(userId);
                executionPhase.setStatus(ExecutionPhaseStatus.CREATED);
                executionPhase.setNumber(phase.getPhase());

                AppNode nodeCopy = mapper.readValue(mapper.writeValueAsString(node), AppNode.class);

                Map<String, Object> cleanedInputs = nodeCopy.getInputs() != null
                        ? nodeCopy.getInputs().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> (e.getValue() instanceof Map<?, ?> map && map.containsKey("id")) ? map.get("id") : e.getValue()
                        ))
                        : new HashMap<>();


                nodeCopy.setInputs(cleanedInputs);
                executionPhase.setNode(mapper.writeValueAsString(node));
                executionPhase.setName(TaskRegistry.get(node.getData().getType().toString()).getLabel());
                phases.add(executionPhase);
            }
        }
        executionPhaseRepository.saveAll(phases);


        // ---------------------------------------------------------------
        finalizeWorkflow(execution, creditsConsumed, executionFailed);
        System.out.println("Environment inside executeWorkflow : " + environment);

        return execution.getId().toString();
    }

    private WorkflowExecution initializeWorkflow(String workflowId, String userId) {
//        WorkflowExecution execution = workflowExecutionRepository.findById(executionId).orElseThrow(() -> new RuntimeException("Workflow not found"));
//        Workflow workflow = workflowRepository.findById(execution.getWorkflowId()).orElseThrow(() -> new RuntimeException("Workflow not found"));
//
//        execution.setStartedAt(LocalDateTime.now());
//        execution.setStatus(WorkflowExecutionStatus.RUNNING);
//        workflowExecutionRepository.save(execution);
//
//        workflow.setLastRunAt(LocalDateTime.now());
//        workflow.setLastRunId(executionId.toString());
//        workflow.setLastRunStatus(WorkflowExecutionStatus.RUNNING);
//        workflowRepository.save(workflow);
//        return execution;

        WorkflowExecution workflowExecution = new WorkflowExecution();
        Workflow workflow = workflowRepository.findById(UUID.fromString(workflowId)).orElseThrow(() -> new RuntimeException("Workflow not found"));

        workflowExecution.setWorkflowId(UUID.fromString(workflowId));
        workflowExecution.setUserId(userId);
        workflowExecution.setStatus(WorkflowExecutionStatus.RUNNING);
        workflowExecution.setTrigger(WorkflowExecutionTrigger.MANUAL);
        workflowExecution.setCreatedAt(LocalDateTime.now());
        workflowExecution.setStartedAt(LocalDateTime.now());
        workflowExecutionRepository.save(workflowExecution);

        workflow.setLastRunAt(LocalDateTime.now());
        workflow.setLastRunId(workflowExecution.getId().toString());
        workflow.setLastRunStatus(WorkflowExecutionStatus.RUNNING);
        workflowRepository.save(workflow);

        return workflowExecution;
    }

    private void initializePhases(UUID executionId) {
        List<ExecutionPhase> executionPhases = executionPhaseRepository.findAllByWorkflowExecutionId(executionId);
        for (ExecutionPhase phase : executionPhases) {
            phase.setStatus(ExecutionPhaseStatus.PENDING);
        }
        executionPhaseRepository.saveAll(executionPhases);
    }

    private void finalizeWorkflow(WorkflowExecution execution, int creditsConsumed, boolean executionFailed) {
        WorkflowExecutionStatus finalStatus = executionFailed ? WorkflowExecutionStatus.FAILED : WorkflowExecutionStatus.COMPLETED;
        execution.setStatus(finalStatus);
        execution.setCompletedAt(LocalDateTime.now());
        execution.setCreditsConsumed(creditsConsumed);
        workflowExecutionRepository.save(execution);

        Workflow workflowAfterExecution = workflowRepository.findByIdAndLastRunId(execution.getWorkflowId(), execution.getId().toString());
        workflowAfterExecution.setLastRunStatus(finalStatus);
        workflowRepository.save(workflowAfterExecution);
    }
}
