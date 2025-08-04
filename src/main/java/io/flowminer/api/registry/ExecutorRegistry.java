package io.flowminer.api.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowminer.api.registry.executor.ExtractTextFromHtmlExecutor;
import io.flowminer.api.registry.executor.LaunchBrowserExecutor;
import io.flowminer.api.registry.executor.PageToHtmlExecutor;
import io.flowminer.api.registry.executor.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
//
//public class ExecutorRegistry {
//    private static final Map<String, TaskExecutor> registry = new HashMap<>();
//
//    static {
//        registry.put("LAUNCH_BROWSER", new LaunchBrowserExecutor());
//        registry.put("PAGE_TO_HTML", new PageToHtmlExecutor());
//        registry.put("EXTRACT_TEXT_FROM_HTML", new ExtractTextFromHtmlExecutor());
//    }
//
//    public static TaskExecutor getExecutor(String type) {
//        return registry.get(type);
//    }
//}
@Component
public class ExecutorRegistry {

    private final Map<String, TaskExecutor> registry = new HashMap<>();

    public ExecutorRegistry(RestTemplate restTemplate) {
        registry.put("LAUNCH_BROWSER", new LaunchBrowserExecutor(restTemplate));
        registry.put("PAGE_TO_HTML", new PageToHtmlExecutor(restTemplate));
        registry.put("EXTRACT_TEXT_FROM_HTML", new ExtractTextFromHtmlExecutor(restTemplate));
    }

    public TaskExecutor getExecutor(String type) {
        return registry.get(type);
    }
}
