package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import org.bukkit.WeatherType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class WeatherRequirementType implements RequirementType {

    @Override
    public boolean checkRequirement(@NotNull RequirementContext context, @NotNull String value) {
        if (context.getWorld() == null) {
            String configLocation = context.getConfigPath();
            if (configLocation == null) {
                configLocation = "N/A";
            }
            EvenMoreFish.getInstance().getLogger().severe("Could not get world for " + configLocation + ", returning false by " +
                    "default. The player may not have been given a fish if you see this message multiple times.");
            return false;
        }
        @NotNull WeatherType weatherType;
        try {
            weatherType = WeatherType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance().getLogger().severe(value + " is not a valid weather type.");
            return false;
        }
        if (context.getWorld().isClearWeather()) {
            return weatherType.equals(WeatherType.CLEAR);
        } else {
            return weatherType.equals(WeatherType.DOWNFALL);
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "WORLD";
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
