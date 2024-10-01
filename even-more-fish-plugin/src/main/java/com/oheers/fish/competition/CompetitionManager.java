package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CompetitionManager {

    private static CompetitionManager instance;

    private Map<String, CompetitionType> registeredTypes;

    private CompetitionManager() {
        registeredTypes = new HashMap<>();
    }

    public static CompetitionManager getInstance() {
        if (instance == null) {
            instance = new CompetitionManager();
        }
        return instance;
    }

    public boolean registerType(@NotNull CompetitionType type) {
        String identifier = type.getIdentifier().toUpperCase();
        if (registeredTypes.containsKey(type.getIdentifier())) {
            return false;
        }
        EMFPlugin.getLogger().info("Registered " + identifier + " CompetitionType by " + type.getAuthor() + " from the plugin " + type.getPlugin().getName());
        registeredTypes.put(identifier, type);
        return true;
    }

    public @Nullable CompetitionType getCompetitionType(@NotNull String identifier) {
        return registeredTypes.get(identifier.toUpperCase());
    }

}
