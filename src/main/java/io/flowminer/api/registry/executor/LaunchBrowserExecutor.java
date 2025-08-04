package io.flowminer.api.registry.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowminer.api.dto.Environment;
import io.flowminer.api.dto.ExecutionEnvironment;
import io.flowminer.api.dto.ScreenshotResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class LaunchBrowserExecutor implements TaskExecutor{
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public LaunchBrowserExecutor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Override
    public boolean execute(ExecutionEnvironment executionEnvironment) {
        System.out.println("Executing Launch Browser Task...");
//        System.out.println("Environment : " + objectMapper.convertValue(executionEnvironment, Map.class));
        String nodeUrl = System.getenv("NODE_PUP_URL") + "/launch";
        String websiteUrl = executionEnvironment.getInput("Website Url");
        Map<String, Object> request = new HashMap<>();
        request.put("url", websiteUrl);
//        request.put("environment", objectMapper.convertValue(executionEnvironment, Map.class));
//        ScreenshotResponse response = restTemplate.postForObject(nodeUrl, request,  ScreenshotResponse.class);

        return true;
    }
}
