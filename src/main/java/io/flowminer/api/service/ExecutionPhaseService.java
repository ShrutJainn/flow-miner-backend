package io.flowminer.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowminer.api.dto.*;
import io.flowminer.api.enums.ExecutionPhaseStatus;
import io.flowminer.api.enums.TaskParamType;
import io.flowminer.api.model.ExecutionPhase;
import io.flowminer.api.registry.ExecutorRegistry;
import io.flowminer.api.registry.TaskRegistry;
import io.flowminer.api.registry.executor.TaskExecutor;
import io.flowminer.api.repository.ExecutionPhaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExecutionPhaseService {

    public final ExecutorRegistry executorRegistry;

    public final ExecutionPhaseRepository executionPhaseRepository;
    ExecutionPhaseService(ExecutionPhaseRepository executionPhaseRepository, ExecutorRegistry executorRegistry) {
        this.executionPhaseRepository = executionPhaseRepository;
        this.executorRegistry = executorRegistry;
    }

    private void finalizePhase(ExecutionPhase phase, boolean success) {
        ExecutionPhaseStatus finalStatus = success ? ExecutionPhaseStatus.COMPLETED : ExecutionPhaseStatus.FAILED;

        phase.setStatus(finalStatus);
        phase.setCompletedAt(LocalDateTime.now());
        executionPhaseRepository.save(phase);
    }
    private boolean executePhase(ExecutionPhase phase, AppNode node, Environment environment) {
        TaskExecutor runFn = executorRegistry.getExecutor(node.getData().getType().toString());
        if(runFn == null) return false;
        ExecutionEnvironment executionEnvironment = createExecutionEnvironment(node, environment);
        return runFn.execute(executionEnvironment);
    }
    private void setupEnvironmentForPhase(AppNode node, Environment environment) {
        Phase phase = new Phase();
        environment.getPhases().put(node.id, phase);

        List<Input> inputs = TaskRegistry.get(node.getData().getType().toString()).getInputs();

        for(Input input : inputs) {
            if(input.getType() == TaskParamType.BROWSER_INSTANCE) continue;
            String inputValue = node.getData().getInputs().get(input.getName());

            if(!inputValue.isEmpty()) {
                phase.getInputs().put(input.getName(), inputValue);
            }
        }
    }
    private ExecutionEnvironment createExecutionEnvironment(AppNode node, Environment environment) {
        return new ExecutionEnvironment() {
            @Override
            public String getInput(String name) {
                Phase phase = environment.getPhases().get(node.getId());
                if(phase != null && phase.getInputs() != null && phase.getInputs().containsKey(name)) {
                    return phase.getInputs().get(name);
                }
                return "";
            }
        };
    }
    public ExecutePhaseResponseDTO executeWorkflowPhase(ExecutionPhase phase, Environment environment) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        AppNode node = objectMapper.readValue(phase.getNode(), AppNode.class);
        // set up the environment
        setupEnvironmentForPhase(node, environment);
        //update phase status
        phase.setStatus(ExecutionPhaseStatus.RUNNING);
        phase.setStartedAt(LocalDateTime.now());
        phase.setInputs(environment.getPhases().get(node.getId()).getInputs().toString());
        executionPhaseRepository.save(phase);

        int creditsConsumed = TaskRegistry.get(node.getData().getType().toString()).getCredits();


        System.out.println("Executing phase " + phase.getName() + " with " + creditsConsumed + " credits required");

        //TODO : Decrement user balance with required credits
//        boolean success = executePhase(phase, node, environment);

        //TODO : Associate status with each phase
        finalizePhase(phase, true);
        return new ExecutePhaseResponseDTO(true);
    }


}




