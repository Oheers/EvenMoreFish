package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class InGameTime implements Requirement {

    public final String configLocation;
    public int minTime, maxTime;

    /**
     * Similar to IRLTime but it uses the in-game time, using the hour * 1000 tick time. If the time is between minTime (inclusively)
     * and maxTime (inclusively) the fish can be caught.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "irl-time:
     *                       for example, "fish.Common.Herring.requirements.ingame-time".
     */
    public InGameTime(@NotNull final String configLocation) {
        this.configLocation = configLocation;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        if (context.getWorld() != null) {
            return context.getWorld().getTime() <= maxTime && context.getWorld().getTime() >= minTime;
        }
        EvenMoreFish.logger.log(Level.SEVERE, "Could not get world for " + configLocation + ", returning false by " +
                "default. The player may not have been given a fish if you see this message multiple times.");
        return false;
    }

    @Override
    public void fetchData() {
        this.minTime = EvenMoreFish.fishFile.getConfig().getInt(configLocation + ".minTime", 0);
        this.maxTime = EvenMoreFish.fishFile.getConfig().getInt(configLocation + ".maxTime", 24000);
    }
}
