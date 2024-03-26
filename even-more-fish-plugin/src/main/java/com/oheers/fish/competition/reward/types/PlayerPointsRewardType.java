package com.oheers.fish.competition.reward.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.reward.RewardType;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlayerPointsRewardType implements RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        int amount;
        try {
            amount = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        if (EvenMoreFish.getInstance().isUsingPlayerPoints()) {
            PlayerPoints.getInstance().getAPI().give(player.getUniqueId(), amount);
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "PLAYER_POINTS";
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
