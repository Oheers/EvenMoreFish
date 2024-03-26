package com.oheers.fish.api.reward;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RewardManager implements Listener {

    private static RewardManager instance = null;

    private final Map<String, RewardType> rewardTypes = new HashMap<>();
    private boolean loaded = false;

    private RewardManager() {}

    public static RewardManager getInstance() {
        if (instance == null) {
            instance = new RewardManager();
        }
        return instance;
    }

    public void load() {
        if (!isLoaded()) {
            Bukkit.getPluginManager().callEvent(new EMFRewardsLoadEvent());
            loaded = true;
        }
    }

    public void unload() {
        if (isLoaded()) {
            this.rewardTypes.clear();
            loaded = false;
            getLogger().info("Unloaded RewardManager");
        }
    }

    public boolean isLoaded() { return loaded; }

    /**
     * Register a custom reward type.
     * @param rewardType The reward type instance you wish to register
     * @return Whether the reward type was added or not
     */
    public boolean registerRewardType(RewardType rewardType) {
        String identifier = rewardType.getIdentifier();
        if (rewardTypes.containsKey(identifier.toUpperCase())) {
            return false;
        }
        rewardType.getPlugin().getLogger().info("Registered " + rewardType.getIdentifier() + " RewardType by " + rewardType.getAuthor());
        rewardTypes.put(identifier.toUpperCase(), rewardType);
        return true;
    }

    public List<RewardType> getRegisteredRewardTypes() {
        return new ArrayList<>(rewardTypes.values());
    }

    public Logger getLogger() {
        return Logger.getLogger("EvenMoreFish");
    }

}
