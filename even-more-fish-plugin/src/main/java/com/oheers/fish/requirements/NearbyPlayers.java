package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NearbyPlayers implements Requirement {

    public final String configLocation;
    public final YamlDocument fileConfig;
    public int nearbyRequirement = 0;

    /**
     * Makes sure the player has enough other players around them to catch the fish.
     * If the player in the context is null, false is returned by default.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "nearby-players:"
     *                       for example, "fish.Common.Herring.requirements.nearby-players".
     * @param fileConfig The file configuration to fetch file data from, this is either the rarities or fish.yml file,
     *                   but it would be possible to use any file, as long as the configLocation is correct.
     */
    public NearbyPlayers(@NotNull final String configLocation, @NotNull final YamlDocument fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        if (context.getPlayer() == null) {
            EvenMoreFish.getInstance().getLogger().warning("Could not find a valid player for " + configLocation + ", returning false by " +
                    "default. The player may not have been given a fish if you see this message multiple times.");
            return false;
        }
        int range = MainConfig.getInstance().getNearbyPlayersRequirementRange();
        long nearbyPlayers = context.getPlayer().getNearbyEntities(range, range, range).stream().filter(entity -> entity instanceof Player).count();
        return nearbyPlayers >= nearbyRequirement;
    }

    @Override
    public void fetchData() {
        this.nearbyRequirement = fileConfig.getInt(configLocation, 0);
    }

}
