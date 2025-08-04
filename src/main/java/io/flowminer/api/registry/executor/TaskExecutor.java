package io.flowminer.api.registry.executor;

import io.flowminer.api.dto.Environment;
import io.flowminer.api.dto.ExecutionEnvironment;

public interface TaskExecutor {
    boolean execute(ExecutionEnvironment environment);
}
