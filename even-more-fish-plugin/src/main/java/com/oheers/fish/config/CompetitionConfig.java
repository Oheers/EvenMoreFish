package com.oheers.fish.config;

import com.oheers.fish.competition.CompetitionType;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class CompetitionConfig extends ConfigBase {

    private static CompetitionConfig instance;

    public CompetitionConfig() {
        super("competitions.yml");
        instance = this;
    }

    public static CompetitionConfig getInstance() {
        return instance;
    }

    public int configVersion() {
        return getConfig().getInt("config-version");
    }

    public List<String> getStringList(String path) {
        return getConfig().getStringList(path);
    }

    public Set<String> getCompetitions() {
        try {
            return getConfig().getConfigurationSection("competitions").getKeys(false);
        } catch (NullPointerException exception) {
            return null;
        }

    }

    public boolean specificDayTimes(String competitionName) {
        return getConfig().getString("competitions." + competitionName + ".days") != null;
    }

    public Set<String> activeDays(String competitionName) {
        return Objects.requireNonNull(getConfig().getConfigurationSection("competitions." + competitionName + ".days")).getKeys(false);
    }

    public List<String> getDayTimes(String competitionName, String day) {
        return getConfig().getStringList("competitions." + competitionName + ".days." + day);
    }

    public int getCompetitionDuration(String competitionName) {
        return getConfig().getInt("competitions." + competitionName + ".duration");
    }

    public CompetitionType getCompetitionType(String competitionName) {
        return CompetitionType.valueOf(getConfig().getString("competitions." + competitionName + ".type"));
    }

    public boolean doingRepeatedTiming(String competitionName) {
        return !getConfig().getStringList("competitions." + competitionName + ".times").isEmpty();
    }

    public List<String> getRepeatedTiming(String competitionName) {
        return getConfig().getStringList("competitions." + competitionName + ".times");
    }

    public boolean hasBlacklistedDays(String competitionName) {
        return !getConfig().getStringList("competitions." + competitionName + ".blacklisted-days").isEmpty();
    }

    public List<String> getBlacklistedDays(String competitionName) {
        return getConfig().getStringList("competitions." + competitionName + ".blacklisted-days");
    }

    public List<String> allowedRarities(String competitionName, boolean adminStart) {
        if (adminStart) {
            return getConfig().getStringList("general.allowed-rarities");
        } else {
            return getConfig().getStringList("competitions." + competitionName + ".allowed-rarities");
        }
    }

    public int getNumberFishNeeded(String competitionName, boolean adminStart) {
        int returning;

        if (adminStart) returning = getConfig().getInt("general.number-needed");
        else returning = getConfig().getInt("competitions." + competitionName + ".number-needed", getConfig().getInt("general.number-needed"));

        if (returning != 0) return returning;
        else return 1;
    }

    public boolean broadcastOnlyRods() {
        return getConfig().getBoolean("general.broadcast-only-rods");
    }

    public List<String> getPositionColours() {
        List<String> returning = getConfig().getStringList("leaderboard.position-colours");

        if (!returning.isEmpty()) return returning;
        else {
            return Arrays.asList("&6", "&e", "&7", "&7", "&8");
        }
    }

    public List<String> getAlertTimes(String competitionName) {
        return getConfig().getStringList("competitions." + competitionName + ".alerts");
    }

    public Set<String> getRewardPositions(String competitionName) {
        ConfigurationSection returning = getConfig().getConfigurationSection("competitions." + competitionName + ".rewards");
        if (returning != null) return returning.getKeys(false);
        else return new HashSet<>();
    }

    public Set<String> getRewardPositions() {
        ConfigurationSection returning = getConfig().getConfigurationSection("rewards");
        if (returning != null) return returning.getKeys(false);
        else return new HashSet<>();
    }

    public String getBarColour(String competitionName) {
        if (competitionName != null) {
            if (getConfig().getString("competitions." + competitionName + ".bossbar-colour") != null) {
                return Objects.requireNonNull(getConfig().getString("competitions." + competitionName + ".bossbar-colour")).toUpperCase();
            } else if (getConfig().getString("general.bossbar-colour") != null) {
                return Objects.requireNonNull(getConfig().getString("general.bossbar-colour")).toUpperCase();
            } else {
                return "GREEN";
            }
        } else {
            if (getConfig().getString("general.bossbar-colour") != null) {
                return Objects.requireNonNull(getConfig().getString("general.bossbar-colour")).toUpperCase();
            } else {
                return "GREEN";
            }
        }
    }

    public String getBarPrefix(String competitionName) {
        if (competitionName != null) {
            if (getConfig().getString("competitions." + competitionName + ".bossbar-prefix") != null) {
                return getConfig().getString("competitions." + competitionName + ".bossbar-prefix");
            } else if (getConfig().getString("general.bossbar-prefix") != null) {
                return getConfig().getString("general.bossbar-prefix");
            } else {
                return "&a&lFishing Contest: ";
            }
        } else {
            if (getConfig().getString("general.bossbar-prefix") != null) {
                return getConfig().getString("general.bossbar-prefix");
            } else {
                return "&a&lFishing Contest: ";
            }
        }
    }

    public int getPlayersNeeded(String competitionName) {
        if (competitionName != null) {
            if (getConfig().getInt("competitions." + competitionName + ".minimum-players") != 0) {
                return getConfig().getInt("competitions." + competitionName + ".minimum-players");
            } else if (getConfig().getInt("general.minimum-players") != 0) {
                return getConfig().getInt("general.minimum-players");
            } else return 1;
        } else {
            if (getConfig().getInt("general.minimum-players") != 0) {
                return getConfig().getInt("general.minimum-players");
            } else return 1;
        }
    }

    public Sound getStartNoise(String competitionName) {
        String stringSound;
        if (competitionName != null) {
            stringSound = getConfig().getString("competitions." + competitionName + ".start-sound", "NONE");
        } else {
            stringSound = getConfig().getString("general.start-sound", "NONE");
        }

        if (!stringSound.equalsIgnoreCase("NONE")) {
            return Sound.valueOf(stringSound);
        } else {
            return null;
        }
    }

    public List<String> getRequiredWorlds() {
        return getConfig().getStringList("general.required-worlds");
    }
}
