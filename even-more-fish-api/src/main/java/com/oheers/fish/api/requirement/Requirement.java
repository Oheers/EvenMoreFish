package com.oheers.fish.api.requirement;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Requirement {

    private final Map<String, String> checkMap;

    public Requirement() {
        checkMap = new HashMap<>();
    }

    public Requirement(@NotNull String identifier, @NotNull List<String> values) {
        checkMap = new HashMap<>();
        processRequirement(identifier, values);
    }

    public Requirement(@NotNull Map<String, List<String>> requirements) {
        checkMap = new HashMap<>();
        requirements.forEach(this::processRequirement);
    }

    public Requirement add(@NotNull String identifier, @NotNull List<String> values) {
        processRequirement(identifier, values);
        return this;
    }

    public Requirement add(@NotNull Map<String, List<String>> requirements) {
        requirements.forEach(this::processRequirement);
        return this;
    }

    private void processRequirement(@NotNull String identifier, @NotNull List<String> values) {
        values.forEach(value -> this.checkMap.put(identifier, value));
    }

    public boolean meetsRequirements(@NotNull RequirementContext context) {
        for (Map.Entry<String, String> entry : checkMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.isEmpty() || value.isEmpty()) {
                EMFPlugin.getLogger().warning("Attempted to process an invalid Requirement. Please check for earlier warnings.");
                continue;
            }
            RequirementType requirementType = RequirementManager.getInstance().getRegisteredRequirements().get(key);
            if (requirementType == null) {
                EMFPlugin.getLogger().warning("Invalid requirement. Possible typo?: " + key + ":" + value);
                continue;
            }
            if (!requirementType.checkRequirement(context, value)) {
                return false;
            }
        }
        return true;
    }

}
