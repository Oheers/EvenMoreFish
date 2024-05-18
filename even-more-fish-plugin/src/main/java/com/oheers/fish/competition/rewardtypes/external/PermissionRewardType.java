package com.oheers.fish.competition.rewardtypes.external;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PermissionRewardType implements RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        Permission permission = EvenMoreFish.getInstance().getPermission();
        if (permission != null) {
            permission.playerAdd(player.getPlayer(), value);
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "PERMISSION";
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
