package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.CompetitionType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;

import java.util.*;
public class CompetitionConfig extends ConfigBase {

    private static CompetitionConfig instance;

    public CompetitionConfig() {
        super("competitions.yml", "competitions.yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }

    public static CompetitionConfig getInstance() {
        return instance;
    }

    public List<String> getStringList(String path) {
        return getConfig().getStringList(path);
    }

    public Set<String> getCompetitions() {
        Section section = getConfig().getSection("competitions");
        return section == null ? Set.of() : section.getRoutesAsStrings(false);
    }

    public boolean specificDayTimes(String competitionName) {
        return getConfig().getString("competitions." + competitionName + ".days") != null;
    }

    public Set<String> activeDays(String competitionName) {
        Section section = getConfig().getSection("competitions." + competitionName + ".days");
        if (section == null) {
            return Set.of();
        }
        return section.getRoutesAsStrings(false);
    }

    public List<String> getDayTimes(String competitionName, String day) {
        return getConfig().getStringList("competitions." + competitionName + ".days." + day);
    }

    public int getCompetitionDuration(String competitionName) {
        return Math.max(1, getConfig().getInt("competitions." + competitionName + ".duration"));
    }

    public List<String> getCompetitionStartCommands(String competitionName) {
        String key = "competitions." + competitionName + ".start-commands";
        if (getConfig().isString(key)) {
            return Collections.singletonList(getConfig().getString(key));
        }

        return getConfig().getStringList(key);
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
        }
        return getConfig().getStringList("competitions." + competitionName + ".allowed-rarities");
    }

    public int getNumberFishNeeded(String competitionName, boolean adminStart) {
        int returning;

        if (adminStart) {
            returning = getConfig().getInt("general.number-needed");
        } else {
            returning = getConfig().getInt("competitions." + competitionName + ".number-needed", getConfig().getInt("general.number-needed"));
        }

        if (returning > 0) {
            return returning;
        } else {
            return 1;
        }
    }

    public boolean broadcastOnlyRods() {
        return getConfig().getBoolean("general.broadcast-only-rods");
    }

    public int getBroadcastRange() {
        return getConfig().getInt("general.broadcast-range", -1);
    }

    public List<String> getPositionColours() {
        return getConfig().getStringList("leaderboard.position-colours", getDefaultPositionColours());
    }

    public List<String> getDefaultPositionColours() {
        return new ArrayList<>(
                List.of("&6", "&e", "&7", "&7", "&8"
                ));
    }

    public List<String> getAlertTimes(String competitionName) {
        return getConfig().getStringList("competitions." + competitionName + ".alerts");
    }

    public Set<String> getRewardPositions(String competitionName) {
        Section returning = getConfig().getSection("competitions." + competitionName + ".rewards");
        if (returning != null) {
            return returning.getRoutesAsStrings(false);
        }

        return new HashSet<>();
    }

    public Set<String> getRewardPositions() {
        Section returning = getConfig().getSection("rewards");
        if (returning != null) {
            return returning.getRoutesAsStrings(false);
        }

        return new HashSet<>();
    }

    public String getBarColour(String competitionName) {
        String barColor;
        if (competitionName != null) {
            barColor = getConfig().getString("competitions." + competitionName + ".bossbar-colour");
        } else {
            barColor = getConfig().getString("general.bossbar-colour");
        }
        if (barColor != null) {
            return barColor.toUpperCase();
        }
        return "GREEN";
    }

    public boolean getShowBar(String competitionName) {
        if (competitionName != null) {
            return getConfig().getBoolean("competitions." + competitionName + ".show-bossbar", true);
        } else {
            return getConfig().getBoolean("general.show-bossbar", true);
        }
    }

    public String getBarPrefix(String competitionName) {
        String barPrefix;

        if (competitionName != null && !competitionName.equals("[admin_started]")) {
            barPrefix = getConfig().getString("competitions." + competitionName + ".bossbar-prefix");
        } else {
            barPrefix = getConfig().getString("general.bossbar-prefix");
        }

        if (barPrefix != null) {
            return barPrefix;
        }

        return "&a&lFishing Contest: ";
    }

    public int getPlayersNeeded(String competitionName) {
        int playersNeeded;
        if (competitionName == null) {
            playersNeeded = getConfig().getInt("competitions." + competitionName + ".minimum-players");
        } else {
            playersNeeded = getConfig().getInt("general.minimum-players");
        }
        if (playersNeeded > 0) {
            return playersNeeded;
        }
        return 1;
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
        }
        return null;
    }

    public List<String> getRequiredWorlds() {
        return getConfig().getStringList("general.required-worlds");
    }
}
