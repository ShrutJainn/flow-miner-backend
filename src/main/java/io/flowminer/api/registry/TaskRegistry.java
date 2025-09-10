package io.flowminer.api.registry;

import io.flowminer.api.dto.Input;
import io.flowminer.api.dto.Output;
import io.flowminer.api.enums.TaskParamType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskRegistry {
    public static final Map<String, TaskDefinition> registry = new HashMap<>();

    static {
        registry.put("LAUNCH_BROWSER", new TaskDefinition(
                true,
                List.of(new Input("Website Url", TaskParamType.STRING, true)),
                List.of(new Output("Web page", "BROWSER_INSTANCE")),
                5,
                "Launch Browser"
        ));
        registry.put("PAGE_TO_HTML", new TaskDefinition(
                false,
                List.of(new Input("Web page", TaskParamType.BROWSER_INSTANCE, true)),
                List.of(new Output("Html", "HTML_STRING")),
                2,
                "Page to HTML"
        ));
        registry.put("EXTRACT_TEXT_FROM_ELEMENT", new TaskDefinition(
                false,
                List.of(new Input("Html", TaskParamType.BROWSER_INSTANCE, true), new Input("Selector", TaskParamType.STRING, true)),
                List.of(new Output("Text", "STRING")),
                2,
                "Extract Text From Element"
        ));
        registry.put("FILL_INPUT", new TaskDefinition(
                false,
                List.of(new Input("Web page", TaskParamType.BROWSER_INSTANCE, true), new Input("Selector", TaskParamType.STRING, true), new Input("Value", TaskParamType.STRING, true)),
                List.of(new Output("Web page", "BROWSER_INSTANCE")),
                1,
                "Fill Input"
        ));
        registry.put("CLICK_ELEMENT", new TaskDefinition(
                false,
                List.of(new Input("Web page", TaskParamType.BROWSER_INSTANCE, true), new Input("Selector", TaskParamType.STRING, true)),
                List.of(new Output("Web page", "BROWSER_INSTANCE")),
                1,
                "Click Element"
        ));
        registry.put("DELIVER_VIA_WEBHOOK", new TaskDefinition(
                false,
                List.of(new Input("Target URL", TaskParamType.STRING, true), new Input("Body", TaskParamType.STRING, true)),
                List.of(new Output()),
                1,
                "Click Element"
        ));
    }
    public static TaskDefinition get(String type){
        return registry.get(type);
    }
}

