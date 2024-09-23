package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import com.oheers.fish.config.MainConfig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class NearbyPlayersRequirementType implements RequirementType {

    @Override
    public boolean checkRequirement(@NotNull RequirementContext context, @NotNull String value) {
        Player player = context.getPlayer();
        if (player == null) {
            String configLocation = context.getConfigPath();
            if (configLocation == null) {
                configLocation = "N/A";
            }
            EvenMoreFish.getInstance().getLogger().warning("Could not find a valid player for " + configLocation + ", returning false by " +
                    "default. The player may not have been given a fish if you see this message multiple times.");
            return false;
        }
        int nearbyRequirement;
        try {
            nearbyRequirement = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            EvenMoreFish.getInstance().getLogger().severe(value + " is not a valid integer");
            return false;
        }
        int range = MainConfig.getInstance().getNearbyPlayersRequirementRange();
        long nearbyPlayers = context.getPlayer().getNearbyEntities(range, range, range).stream().filter(entity -> entity instanceof Player).count();
        return nearbyPlayers >= nearbyRequirement;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "NEARBY-PLAYERS";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FireML";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
