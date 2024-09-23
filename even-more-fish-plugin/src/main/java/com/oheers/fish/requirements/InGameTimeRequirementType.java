package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// TODO auto-update configs to reflect the new format required for this
public class InGameTimeRequirementType implements RequirementType {

    @Override
    public boolean checkRequirement(@NotNull RequirementContext context, @NotNull List<String> values) {
        World world = context.getWorld();
        if (world == null) {
            String configLocation = context.getConfigPath();
            if (configLocation == null) {
                configLocation = "N/A";
            }
            EvenMoreFish.getInstance().getLogger().severe("Could not get world for " + configLocation + ", returning false by " +
                    "default. The player may not have been given a fish if you see this message multiple times.");
            return false;
        }
        for (String value : values) {
            String[] split = value.split("-");
            int minTime;
            int maxTime;
            try {
                minTime = Integer.parseInt(split[0]);
                maxTime = Integer.parseInt(split[1]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
                EvenMoreFish.getInstance().getLogger().severe(value + " is not a valid ingame time format. Using the defaults.");
                minTime = 0;
                maxTime = 24000;
            }
            if (world.getTime() <= maxTime && world.getTime() >= minTime) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "INGAME-TIME";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
