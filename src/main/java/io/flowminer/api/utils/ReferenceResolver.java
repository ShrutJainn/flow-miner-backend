package io.flowminer.api.utils;

import io.flowminer.api.dto.Environment;
import io.flowminer.api.dto.Phase;

import java.util.HashMap;
import java.util.Map;

public class ReferenceResolver {
    public static Map<String, Object> resolveInputs(Map<String, String> inputs, Environment env) {
        if (inputs == null) return new HashMap<>();

        Map<String, Object> resolved = new HashMap<>();

        for (Map.Entry<String, String> entry : inputs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value != null && value.startsWith("$ref:")) {
                // Extract referenced phase id
                String refId = value.substring(5);

                // Look up referenced phase in environment
                Phase refPhase = env.getPhases().get(refId);
                if (refPhase != null && refPhase.getOutputs() != null) {
                    resolved.put(key, refPhase.getOutputs());
                } else {
                    // fallback to raw ref if something missing
                    resolved.put(key, value);
                }
            } else {
                resolved.put(key, value);
            }
        }
        System.out.println("resolved : " + resolved);

        return resolved;
    }
}
