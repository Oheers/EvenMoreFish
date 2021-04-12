package com.oheers.fish.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

public class MainConfig {

    private FileConfiguration config = Bukkit.getPluginManager().getPlugin("EvenMoreFish").getConfig();

    public int getCompetitionDuration() {
        return config.getInt("competitions.duration");
    }

    public List<String> getCompetitionTimes() {
        return config.getStringList("competitions.times");
    }

    public boolean doingRandomDurability() {
        return config.getBoolean("random-durability");
    }

    public boolean isDatabaseOnline() {
        return config.getBoolean("database");
    }

    public boolean isCompetitionUnique() {
        return config.getBoolean("fish-only-in-competition");
    }

    public List<String> getPositionRewards(String position) {
        return config.getStringList("competitions.winnings." + position);
    }

    public Set<String> getTotalRewards() {
        return config.getConfigurationSection("competitions.winnings").getKeys(false);
    }

    public boolean getEnabled() {
        return config.getBoolean("enabled");
    }

}
