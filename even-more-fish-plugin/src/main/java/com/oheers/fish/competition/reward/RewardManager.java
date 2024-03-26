package com.oheers.fish.competition.reward;

import com.oheers.fish.competition.reward.types.*;

import java.util.*;

public class RewardManager {

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
            new CommandRewardType().register();
            new EffectRewardType().register();
            new HealthRewardType().register();
            new HungerRewardType().register();
            new ItemRewardType().register();
            new MessageRewardType().register();
            new MoneyRewardType().register();
            new PermissionRewardType().register();
            new PlayerPointsRewardType().register();
            new EXPRewardType().register();
            loaded = true;
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

}
