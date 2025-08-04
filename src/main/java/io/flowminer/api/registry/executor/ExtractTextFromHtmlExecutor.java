package io.flowminer.api.registry.executor;

import io.flowminer.api.dto.Environment;
import io.flowminer.api.dto.ExecutionEnvironment;
import org.springframework.web.client.RestTemplate;

public class ExtractTextFromHtmlExecutor implements TaskExecutor{
    private final RestTemplate restTemplate;
    public ExtractTextFromHtmlExecutor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Override
    public boolean execute(ExecutionEnvironment environment) {
        System.out.println("Executing Extract Text from Html Task...");
        return true;
    }
}
