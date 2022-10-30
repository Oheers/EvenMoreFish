package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class World implements Requirement {

    public final String configLocation;
    public final List<String> worlds = new ArrayList<>();

    /**
     * Lets the fish only be sent if the hook is in one of the allowed worlds. If the world in the context is null then
     * false will be returned by default.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "world:"
     *                       for example, "fish.Common.Herring.requirements.world".
     */
    public World(@NotNull final String configLocation) {
        this.configLocation = configLocation;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        if (context.getWorld() != null) {
            return worlds.contains(context.getWorld().getName());
        }
        EvenMoreFish.logger.log(Level.SEVERE, "Could not get world for " + configLocation + ", returning false by " +
                "default. The player may not have been given a fish if you see this message multiple times.");
        return false;
    }

    @Override
    public void fetchData() {
        this.worlds.addAll(EvenMoreFish.fishFile.getConfig().getStringList(configLocation));
    }


}
