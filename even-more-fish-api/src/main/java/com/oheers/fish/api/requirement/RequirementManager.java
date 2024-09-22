package com.oheers.fish.api.requirement;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.reward.EMFRewardsLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;
import uk.firedev.daisylib.Loggers;
import uk.firedev.daisylib.local.DaisyLib;
import uk.firedev.daisylib.requirement.requirements.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequirementManager implements Listener {

    private static RequirementManager instance;

    private final Map<String, RequirementType> requirements = new HashMap<>();
    private boolean loaded = false;

    private RequirementManager() {}

    public static RequirementManager getInstance() {
        if (instance == null) {
            instance = new RequirementManager();
        }
        return instance;
    }

    public void load() {
        if (!isLoaded()) {
            Bukkit.getPluginManager().callEvent(new EMFRequirementsLoadEvent());
            loaded = true;
            EMFPlugin.getLogger().info("Loaded RequirementManager");
        }
    }

    public void unload() {
        if (isLoaded()) {
            this.requirements.clear();
            loaded = false;
            EMFPlugin.getLogger().info("Unloaded RequirementManager");
        }
    }

    public boolean isLoaded() { return loaded; }

    /**
     * Register a custom requirement.
     * @param requirementType The requirement type instance you wish to register
     * @return Whether the requirement type was added or not
     */
    public boolean registerRequirement(RequirementType requirementType) {
        String identifier = requirementType.getIdentifier().toUpperCase();
        if (requirements.containsKey(identifier)) {
            return false;
        }
        EMFPlugin.getLogger().info("Registered " + identifier + " Requirement by " + requirementType.getAuthor() + " from the plugin " + requirementType.getPlugin().getName());
        requirements.put(identifier, requirementType);
        return true;
    }

    public Map<String, RequirementType> getRegisteredRequirements() { return new HashMap<>(requirements); }

    public List<RequirementType> getRegisteredRequirementTypes() {
        return new ArrayList<>(requirements.values());
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        Bukkit.getPluginManager().callEvent(new EMFRequirementsLoadEvent());
    }

}
