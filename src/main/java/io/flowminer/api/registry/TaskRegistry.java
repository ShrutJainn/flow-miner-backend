package io.flowminer.api.registry;

import io.flowminer.api.dto.Input;
import io.flowminer.api.dto.Output;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskRegistry {
    public static final Map<String, TaskDefinition> registry = new HashMap<>();

//    static {
//        registry.put("LAUNCH_BROWSER", new TaskDefinition(true, List.of(new Input("Website Url", true))));
//        registry.put("PAGE_TO_HTML", new TaskDefinition(false, List.of(
//                new Input("Web page", true)
//        )));
//        registry.put("EXTRACT_TEXT_FROM_ELEMENT", new TaskDefinition(false, List.of(
//                new Input("Html", true),
//                new Input("Selector", true)
//        )));
//    }

    static {
        registry.put("LAUNCH_BROWSER", new TaskDefinition(
                true,
                List.of(new Input("Website Url", true)),
                List.of(new Output("Web page", "BROWSER_INSTANCE")),
                5,
                "Launch Browser"
        ));
        registry.put("PAGE_TO_HTML", new TaskDefinition(
                false,
                List.of(new Input("Web page", true)),
                List.of(new Output("Html", "HTML_STRING")),
                3,
                "Page to HTML"
        ));
        registry.put("EXTRACT_TEXT_FROM_ELEMENT", new TaskDefinition(
                false,
                List.of(new Input("Html", true), new Input("Selector", true)),
                List.of(new Output("Text", "STRING")),
                4,
                "Extract Text From Element"
        ));
    }
    public static TaskDefinition get(String type){
        return registry.get(type);
    }
}
