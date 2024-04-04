package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class InGameTime implements Requirement {

    public final String configLocation;
    public final FileConfiguration fileConfig;
    public int minTime, maxTime;

    /**
     * Similar to IRLTime but it uses the in-game time, using the hour * 1000 tick time. If the time is between minTime (inclusively)
     * and maxTime (inclusively) the fish can be caught.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "ingame-time:"
     *                       for example, "fish.Common.Herring.requirements.ingame-time".
     * @param fileConfig The file configuration to fetch file data from, this is either the rarities or fish.yml file,
     *                   but it would be possible to use any file, as long as the configLocation is correct.
     */
    public InGameTime(@NotNull final String configLocation, @NotNull final FileConfiguration fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        if (context.getWorld() != null) {
            return context.getWorld().getTime() <= maxTime && context.getWorld().getTime() >= minTime;
        }
        EvenMoreFish.getInstance().getLogger().severe("Could not get world for " + configLocation + ", returning false by " +
                "default. The player may not have been given a fish if you see this message multiple times.");
        return false;
    }

    @Override
    public void fetchData() {
        this.minTime = fileConfig.getInt(configLocation + ".minTime", 0);
        this.maxTime = fileConfig.getInt(configLocation + ".maxTime", 24000);
    }
}
