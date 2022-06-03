package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Biome implements Requirement {

	public final String configLocation;
	public final List<org.bukkit.block.Biome> biomes = new ArrayList<>();

	@Override
	public boolean requirementMet(RequirementContext context) {
		if (context.getWorld() != null) {
			return biomes.contains(context.getWorld().getBiome(context.getLocation()));
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

	/**
	 * Checks the world for the current biome the player is stood in to figure out whether to give the player the fish
	 * or not. Will not work if the world is null. It also takes the biomes in as a list so you can have multiple be
	 * whitelisted for the user.
	 *
	 * @param configLocation The location that data regarding this should be found. It should cut off after "irl-time:
	 *                       for example, "fish.Common.Herring.requirements.biome".
	 */
	public Biome(@NotNull final String configLocation) {
		this.configLocation = configLocation;
		fetchData();
	}
}
