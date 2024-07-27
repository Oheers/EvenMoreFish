package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.MainConfig;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BiomeSet implements Requirement {

    public final String configLocation;
    public final YamlDocument fileConfig;
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
    public BiomeSet(@NotNull final String configLocation, @NotNull final YamlDocument fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        if (context.getWorld() != null) {
            return biomes.contains(context.getWorld().getBiome(context.getLocation().getBlockX(), context.getLocation().getBlockY(), context.getLocation().getBlockZ()));
        }
        EvenMoreFish.getInstance().getLogger().severe("Could not get world for " + configLocation + ", returning false by " +
                "default. The player may not have been given a fish if you see this message multiple times.");
        return false;
    }

    @Override
    public void fetchData() {
        // returns the biomes from the sets found in the "biome-sets:" section of the fish.yml
        Map<String, List<org.bukkit.block.Biome>> biomeSets = MainConfig.getInstance().getBiomeSets();
        for (String biomeSet : FishFile.getInstance().getConfig().getStringList(configLocation)) {
            List<org.bukkit.block.Biome> biomeList = biomeSets.get(biomeSet);
            if (biomeList == null) {
                EvenMoreFish.getInstance().getLogger().severe(biomeSet + " is not a valid biome set, found when loading in one of your fish.");
                return;
            }
            this.biomes.addAll(biomeList);
        }
    }
}
