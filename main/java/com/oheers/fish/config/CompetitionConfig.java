package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.CompetitionType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CompetitionConfig {

    FileConfiguration config = EvenMoreFish.competitionFile.getConfig();

    public int configVersion() {
        return config.getInt("config-version");
    }

    public Set<String> getCompetitions() {
        return Objects.requireNonNull(config.getConfigurationSection("competitions")).getKeys(false);
    }

    public boolean specificDayTimes(String competitionName) {
        return config.getString("competitions." + competitionName + ".days") != null;
    }

    public Set<String> activeDays(String competitionName) {
        return Objects.requireNonNull(config.getConfigurationSection("competitions." + competitionName + ".days")).getKeys(false);
    }

    public List<String> getDayTimes(String competitionName, String day) {
        return config.getStringList("competitions." + competitionName + ".days." + day);
    }

    public int getCompetitionDuration(String competitionName) {
        return config.getInt("competitions." + competitionName + ".duration");
    }

    public CompetitionType getCompetitionType(String competitionName) {
        return CompetitionType.valueOf(config.getString("competitions." + competitionName + ".type"));
    }

    public boolean doingRepeatedTiming(String competitionName) {
        return config.getStringList("competitions." + competitionName + ".times").size() != 0;
    }

    public List<String> getRepeatedTiming(String competitionName) {
        return config.getStringList("competitions." + competitionName + ".times");
    }

    public boolean hasBlacklistedDays(String competitionName) {
        return config.getStringList("competitions." + competitionName + ".blacklisted-days").size() != 0;
    }

    public List<String> getBlacklistedDays(String competitionName) {
        return config.getStringList("competitions." + competitionName + ".blacklisted-days");
    }

    public List<String> allowedRarities(String competitionName, boolean adminStart) {
        if (adminStart) {
            return config.getStringList("general.allowed-rarities");
        } else {
            System.out.println("comp: " + competitionName);
            return config.getStringList("competitions." + competitionName + ".allowed-rarities");
        }

    }
}
