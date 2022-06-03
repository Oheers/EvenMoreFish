package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.WeatherType;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class Weather implements Requirement {

	public WeatherType allowedWeather;
	public final String configLocation;

	@Override
	public boolean requirementMet(RequirementContext context) {
		if (context.getWorld() != null) {
			if (context.getWorld().isClearWeather()) return allowedWeather == WeatherType.CLEAR;
			else return allowedWeather == WeatherType.DOWNFALL;
		}
		EvenMoreFish.logger.log(Level.SEVERE, "Could not get world for " + configLocation + ", returning false by " +
				"default. The player may not have been given a fish if you see this message multiple times.");
		return false;
	}

	@Override
	public void fetchData() {
		try {
			this.allowedWeather = WeatherType.valueOf(EvenMoreFish.fishFile.getConfig().getString(configLocation));
		} catch (IllegalArgumentException | NullPointerException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, EvenMoreFish.fishFile.getConfig().getString(configLocation) + " is not a valid weather type.");
		}
	}

	/**
	 * Lets the fish only be sent if there is a certain type of weather present. To be honest when I started making this
	 * I thought there were more weather types, however sadly there aren't, so it checks if there's clear weather and
	 * returns allowedWeather == WeatherType.CLEAR, then it does the opposite too to check for "downpour", if the world
	 * in the context is null then the requirement will always be false and an error will be sent to console.
	 *
	 * @param configLocation The location that data regarding this should be found. It should cut off after "irl-time:
	 *                       for example, "fish.Common.Herring.requirements.weather".
	 */
	public Weather(@NotNull final String configLocation) {
		this.configLocation = configLocation;
		fetchData();
	}
}
