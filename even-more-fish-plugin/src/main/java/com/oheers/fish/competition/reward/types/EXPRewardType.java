package com.oheers.fish.competition.reward.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class EXPRewardType implements RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        int experience;
        try {
            experience = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        player.giveExp(experience);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "EXP";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FireML";
    }

    @Override
    public @NotNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
