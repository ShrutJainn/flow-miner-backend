package io.flowminer.api.registry.executor;

import io.flowminer.api.dto.Environment;
import io.flowminer.api.dto.ExecutionEnvironment;
import org.springframework.web.client.RestTemplate;

public class PageToHtmlExecutor implements TaskExecutor{
    private final RestTemplate restTemplate;
    public PageToHtmlExecutor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Override
    public boolean execute(ExecutionEnvironment environment) {
        System.out.println("Executing Page to Html Task...");
        return true;
    }
}
