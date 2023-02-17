package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Biome implements Requirement {

    public final String configLocation;
    public final FileConfiguration fileConfig;
    public final List<org.bukkit.block.Biome> biomes = new ArrayList<>();

    /**
     * Checks the world for the current biome the player is stood in to figure out whether to give the player the fish
     * or not. Will not work if the world is null. It also takes the biomes in as a list, so you can have multiple be
     * whitelisted for the user.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "biome:"
     *                       for example, "fish.Common.Herring.requirements.biome".
     * @param fileConfig The file configuration to fetch file data from, this is either the rarities or fish.yml file,
     *                   but it would be possible to use any file, as long as the configLocation is correct.
     */
    public Biome(@NotNull final String configLocation, @NotNull final FileConfiguration fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        if (context.getWorld() != null) {
            return biomes.contains(context.getWorld().getBiome(context.getLocation().getBlockX(), context.getLocation().getBlockY(), context.getLocation().getBlockZ()));
        }
        EvenMoreFish.logger.log(Level.SEVERE, "Could not get world for " + configLocation + ", returning false by " +
                "default. The player may not have been given a fish if you see this message multiple times.");
        return false;
    }

    @Override
    public void fetchData() {
        // returns the biomes found in the "biomes:" section of the fish.yml
        for (String biome : EvenMoreFish.fishFile.getConfig().getStringList(configLocation)) {
            try {
                this.biomes.add(org.bukkit.block.Biome.valueOf(biome));
            } catch (IllegalArgumentException iae) {
                EvenMoreFish.logger.log(Level.SEVERE, biome + " is not a valid biome, found when loading in one of your fish.");
            }
        }
    }
}
