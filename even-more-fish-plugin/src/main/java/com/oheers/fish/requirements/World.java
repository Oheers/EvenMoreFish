package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class World implements Requirement {

    public final String configLocation;
    public final YamlDocument fileConfig;
    public final List<String> worlds = new ArrayList<>();

    /**
     * Lets the fish only be sent if the hook is in one of the allowed worlds. If the world in the context is null then
     * false will be returned by default.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "world:"
     *                       for example, "fish.Common.Herring.requirements.world".
     * @param fileConfig The file configuration to fetch file data from, this is either the rarities or fish.yml file,
     *                   but it would be possible to use any file, as long as the configLocation is correct.
     */
    public World(@NotNull final String configLocation, @NotNull final YamlDocument fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        if (context.getWorld() != null) {
            return worlds.contains(context.getWorld().getName());
        }
        EvenMoreFish.getInstance().getLogger().severe("Could not get world for " + configLocation + ", returning false by " +
                "default. The player may not have been given a fish if you see this message multiple times.");
        return false;
    }

    @Override
    public void fetchData() {
        this.worlds.addAll(fileConfig.getStringList(configLocation));
    }


}
