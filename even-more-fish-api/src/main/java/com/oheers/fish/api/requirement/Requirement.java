package com.oheers.fish.api.requirement;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Requirement {

    private final Map<String, String> checkMap;

    public Requirement(@NotNull String identifier) {
        checkMap = new HashMap<>();
        processIdentifier(identifier);
    }

    public Requirement(@NotNull List<String> identifiers) {
        checkMap = new HashMap<>();
        identifiers.forEach(this::processIdentifier);
    }

    private void processIdentifier(@NotNull String identifier) {
        String[] split = identifier.split(":");
        try {
            this.checkMap.putIfAbsent(split[0], String.join(":", Arrays.copyOfRange(split, 1, split.length)));
        } catch (ArrayIndexOutOfBoundsException ex) {
            EMFPlugin.getLogger().warning("Broken requirement " + identifier);
        }
    }

    public boolean meetsRequirements(@NotNull Player player) {
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
            if (!requirementType.checkRequirement(player, value)) {
                return false;
            }
        }
        return true;
    }

}
