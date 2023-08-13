package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.CompetitionType;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CompetitionConfig {

    private final EvenMoreFish plugin;
    private FileConfiguration config;

    public CompetitionConfig(EvenMoreFish plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File competitionsFile = new File(this.plugin.getDataFolder(), "competitions.yml");

        if (!competitionsFile.exists()) {
            competitionsFile.getParentFile().mkdirs();
            this.plugin.saveResource("competitions.yml", false);
        }

        this.config = new YamlConfiguration();

        try {
            this.config.load(competitionsFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public int configVersion() {
        return config.getInt("config-version");
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public Set<String> getCompetitions() {
        try {
            return config.getConfigurationSection("competitions").getKeys(false);
        } catch (NullPointerException exception) {
            return null;
        }

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
        return !config.getStringList("competitions." + competitionName + ".times").isEmpty();
    }

    public List<String> getRepeatedTiming(String competitionName) {
        return config.getStringList("competitions." + competitionName + ".times");
    }

    public boolean hasBlacklistedDays(String competitionName) {
        return !config.getStringList("competitions." + competitionName + ".blacklisted-days").isEmpty();
    }

    public List<String> getBlacklistedDays(String competitionName) {
        return config.getStringList("competitions." + competitionName + ".blacklisted-days");
    }

    public List<String> allowedRarities(String competitionName, boolean adminStart) {
        if (adminStart) {
            return config.getStringList("general.allowed-rarities");
        } else {
            return config.getStringList("competitions." + competitionName + ".allowed-rarities");
        }
    }

    public int getNumberFishNeeded(String competitionName, boolean adminStart) {
        int returning;

        if (adminStart) returning = config.getInt("general.number-needed");
        else returning = config.getInt("competitions." + competitionName + ".number-needed", config.getInt("general.number-needed"));

        if (returning != 0) return returning;
        else return 1;
    }

    public boolean broadcastOnlyRods() {
        return config.getBoolean("general.broadcast-only-rods");
    }

    public List<String> getPositionColours() {
        List<String> returning = config.getStringList("leaderboard.position-colours");

        if (!returning.isEmpty()) return returning;
        else {
            return Arrays.asList("&6", "&e", "&7", "&7", "&8");
        }
    }

    public List<String> getAlertTimes(String competitionName) {
        return config.getStringList("competitions." + competitionName + ".alerts");
    }

    public Set<String> getRewardPositions(String competitionName) {
        ConfigurationSection returning = config.getConfigurationSection("competitions." + competitionName + ".rewards");
        if (returning != null) return returning.getKeys(false);
        else return new HashSet<>();
    }

    public Set<String> getRewardPositions() {
        ConfigurationSection returning = config.getConfigurationSection("rewards");
        if (returning != null) return returning.getKeys(false);
        else return new HashSet<>();
    }

    public String getBarColour(String competitionName) {
        if (competitionName != null) {
            if (config.getString("competitions." + competitionName + ".bossbar-colour") != null) {
                return Objects.requireNonNull(config.getString("competitions." + competitionName + ".bossbar-colour")).toUpperCase();
            } else if (config.getString("general.bossbar-colour") != null) {
                return Objects.requireNonNull(config.getString("general.bossbar-colour")).toUpperCase();
            } else {
                return "GREEN";
            }
        } else {
            if (config.getString("general.bossbar-colour") != null) {
                return Objects.requireNonNull(config.getString("general.bossbar-colour")).toUpperCase();
            } else {
                return "GREEN";
            }
        }
    }

    public String getBarPrefix(String competitionName) {
        if (competitionName != null) {
            if (config.getString("competitions." + competitionName + ".bossbar-prefix") != null) {
                return config.getString("competitions." + competitionName + ".bossbar-prefix");
            } else if (config.getString("general.bossbar-prefix") != null) {
                return config.getString("general.bossbar-prefix");
            } else {
                return "&a&lFishing Contest: ";
            }
        } else {
            if (config.getString("general.bossbar-prefix") != null) {
                return config.getString("general.bossbar-prefix");
            } else {
                return "&a&lFishing Contest: ";
            }
        }
    }

    public int getPlayersNeeded(String competitionName) {
        if (competitionName != null) {
            if (config.getInt("competitions." + competitionName + ".minimum-players") != 0) {
                return config.getInt("competitions." + competitionName + ".minimum-players");
            } else if (config.getInt("general.minimum-players") != 0) {
                return config.getInt("general.minimum-players");
            } else return 1;
        } else {
            if (config.getInt("general.minimum-players") != 0) {
                return config.getInt("general.minimum-players");
            } else return 1;
        }
    }

    public Sound getStartNoise(String competitionName) {
        String stringSound;
        if (competitionName != null) {
            stringSound = config.getString("competitions." + competitionName + ".start-sound", "NONE");
        } else {
            stringSound = config.getString("general.start-sound", "NONE");
        }

        if (!stringSound.equalsIgnoreCase("NONE")) {
            return Sound.valueOf(stringSound);
        } else {
            return null;
        }
    }

    public List<String> getRequiredWorlds() {
        return config.getStringList("general.required-worlds");
    }
}
