package io.flowminer.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowminer.api.dto.*;
import io.flowminer.api.enums.ExecutionPhaseStatus;
import io.flowminer.api.enums.WorkflowEnum;
import io.flowminer.api.enums.WorkflowExecutionStatus;
import io.flowminer.api.enums.WorkflowExecutionTrigger;
import io.flowminer.api.model.EnvironmentModel;
import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.model.Workflow;
import io.flowminer.api.model.WorkflowExecution;
import io.flowminer.api.registry.TaskRegistry;
import io.flowminer.api.repository.EnvironmentRepository;
import io.flowminer.api.repository.ExecutionPhaseRepository;
import io.flowminer.api.repository.WorkflowExecutionRepository;
import io.flowminer.api.repository.WorkflowRepository;
import io.flowminer.api.service.FlowToExecutionPlanService;
import io.flowminer.api.service.RedisService;
import io.flowminer.api.service.WorkflowService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.coyote.Response;
import org.hibernate.jdbc.Work;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    public final WorkflowService workflowService;
    public final WorkflowRepository workflowRepository;
    public final WorkflowExecutionRepository workflowExecutionRepository;
    public final FlowToExecutionPlanService flowToExecutionPlanService;
    public final ExecutionPhaseRepository executionPhaseRepository;
    public final ObjectMapper objectMapper;
    public final EnvironmentRepository environmentRepository;
    public final RestTemplate restTemplate;
    public final RedisService redisService;

    public WorkflowController(WorkflowService workflowService, RedisService redisService, RestTemplate restTemplate, WorkflowRepository workflowRepository, WorkflowExecutionRepository workflowExecutionRepository, FlowToExecutionPlanService flowToExecutionPlanService, ExecutionPhaseRepository executionPhaseRepository, ObjectMapper objectMapper, EnvironmentRepository environmentRepository) {
        this.workflowService = workflowService;
        this.workflowRepository = workflowRepository;
        this.flowToExecutionPlanService = flowToExecutionPlanService;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.executionPhaseRepository = executionPhaseRepository;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.environmentRepository = environmentRepository;
        this.restTemplate = restTemplate;
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

        if (workflow.isEmpty()) throw new RuntimeException("Workflow not found or doesn't belong to the user");

        workflowRepository.delete(workflow.get());
    }

    @GetMapping("/{workflowId}")
    public Workflow getWorkflowById(@PathVariable String workflowId) {
        Optional<Workflow> workflow = workflowRepository.findById(UUID.fromString(workflowId));
        if (workflow.isEmpty()) throw new RuntimeException("Workflow not found");

        return workflow.get();
    }

    @PostMapping("/update/{id}")
    public Workflow updateWorkflow(@PathVariable String id, @RequestBody UpdateWorkflowDTO req) {
        Optional<Workflow> workflowOpt = workflowRepository.findByIdAndUserId(UUID.fromString(id), req.userId);

        if (workflowOpt.isEmpty()) throw new RuntimeException("Workflow not found or does not belong to the user");
        Workflow workflow = workflowOpt.get();
        if (workflow.getStatus() != WorkflowEnum.DRAFT) throw new RuntimeException("Workflow is not a draft");
        workflow.setDefinition(req.definition);
        workflowRepository.save(workflow);
        return workflow;
    }

    @PostMapping("/generate-plan")
    public ResponseEntity<Map<String, Object>> generatePlan(@RequestBody GenerateWorkflowRequestDTO req) throws JsonProcessingException {
        Optional<Workflow> workflowOpt = workflowRepository.findByIdAndUserId(UUID.fromString(req.workflowId), req.userId);
        if (workflowOpt.isEmpty())
            throw new RuntimeException("Workflow not found or doesn't belong to the specified user");
        Workflow workflow = workflowOpt.get();

        if (req.flowDefinition.isEmpty()) throw new RuntimeException("Flow definition is not defined");

        ObjectMapper mapper = new ObjectMapper();
        FlowDefinitionDTO flowDefinition = mapper.readValue(req.flowDefinition, FlowDefinitionDTO.class);
        List<AppNode> nodes = flowDefinition.getNodes();
        List<Edge> edges = flowDefinition.getEdges();

        FlowToExecutionPlanResponse response = flowToExecutionPlanService.generatePlan(nodes, edges);

        Environment environment = new Environment();
        for (WorkflowExecutionPlanPhase phase : response.getExecutionPlan().getPhases()) {
            for (AppNode node : phase.getNodes()) {
                environment.getPhases().put(node.getId(), new Phase(node.getData().getType(), node.getData().getInputs(), node.getData().getOutputs()));
            }
        }
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(environment);
        String plan = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getExecutionPlan());
        System.out.println("Environment generated : " + json);
        System.out.println("Execution plan : " + plan);

        ObjectMapper cleanMapper = objectMapper.copy();
        cleanMapper.deactivateDefaultTyping();


        String redisKey = "env:" + workflow.getId();
        redisService.saveEnvironmentOnRedis(redisKey, cleanMapper.writerWithDefaultPrettyPrinter().writeValueAsString(environment));

        String nodeUrl = System.getenv("NODE_PUP_URL") + "/execute/" + workflow.getId().toString();


        try{
            ResponseEntity<Map> environmentFromNodeServer = restTemplate.postForEntity(nodeUrl, null, Map.class);

            if(environmentFromNodeServer.getStatusCode().is2xxSuccessful()) {
                Boolean success = (Boolean) environmentFromNodeServer.getBody().get("success");

                if(Boolean.TRUE.equals(success)) {
                    String updatedEnv = redisService.getEnvironment(redisKey);

                    Map<String,Object> envMap = new ObjectMapper().readValue(updatedEnv, Map.class);
                    System.out.println("updated environment from redis : " + envMap);

                } else {
                    System.err.println("Node server error : " + environmentFromNodeServer.getBody().get("error"));
                }
            } else {
                System.err.println("Node server returned status : " + environmentFromNodeServer.getStatusCode());
            }

        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }


        WorkflowExecution workflowExecution = new WorkflowExecution();
        workflowExecution.setWorkflowId(UUID.fromString(req.workflowId));
        workflowExecution.setUserId(req.userId);
        workflowExecution.setStatus(WorkflowExecutionStatus.PENDING);
        workflowExecution.setTrigger(WorkflowExecutionTrigger.MANUAL);
        workflowExecution.setCreatedAt(LocalDateTime.now());
        workflowExecution.setStartedAt(LocalDateTime.now());

        WorkflowExecution savedExecution = workflowExecutionRepository.save(workflowExecution);

        List<ExecutionPhase> phases = new ArrayList<>();

        for (WorkflowExecutionPlanPhase phase : response.getExecutionPlan().getPhases()) {
            for (AppNode node : phase.getNodes()) {
                ExecutionPhase executionPhase = new ExecutionPhase();
                executionPhase.setWorkflowExecutionId(savedExecution.getId());
                executionPhase.setUserId(req.userId);
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

        Map<String, Object> res = new HashMap<>();
        res.put("workflowExecutionId", savedExecution.getId());
        res.put("phasesCreated", phases.size());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/execution/{executionId}")
    public ResponseEntity<WorkflowExecutionWithPhasesDTO> getWorkflowExecutionWithPhases(@PathVariable String executionId) {
        WorkflowExecution workflowExecution = workflowExecutionRepository.findById(UUID.fromString(executionId)).orElseThrow(() -> new RuntimeException("Execution not found"));
        List<ExecutionPhase> phases = executionPhaseRepository.findAllByWorkflowExecutionId(UUID.fromString(executionId));

        phases.sort(Comparator.comparingInt(ExecutionPhase::getNumber));
        WorkflowExecutionWithPhasesDTO response = new WorkflowExecutionWithPhasesDTO(workflowExecution, phases);
        return ResponseEntity.ok(response);
    }


    @Data
    @AllArgsConstructor
    public static class GenerateWorkflowRequestDTO {
        public String workflowId;
        public String userId;
        public String flowDefinition;
    }

    @Data
    @AllArgsConstructor
    public static class WorkflowExecutionWithPhasesDTO {
        private WorkflowExecution workflowExecution;
        private List<ExecutionPhase> phases;
    }
}
