package com.oheers.fish.competition.reward;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public interface RewardType {

    default boolean isApplicable(@NotNull String key) {
        return key.equalsIgnoreCase(getIdentifier());
    }

    void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation);

    @NotNull String getIdentifier();

    @NotNull String getAuthor();

    @NotNull JavaPlugin getPlugin();

    default boolean register() {
        return RewardManager.getInstance().registerRewardType(this);
    }

}