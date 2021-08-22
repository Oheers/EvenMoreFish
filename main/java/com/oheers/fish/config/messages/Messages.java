package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class Messages {

    FileConfiguration config = EvenMoreFish.messageFile.getConfig();

    public int configVersion() {
        return config.getInt("config-version");
    }

    public String getSellMessage() {
        return getSTDPrefix() + config.getString("fish-sale");
    }

    public String getWorthGUIName() {
        return config.getString("worth-gui-name");
    }

    public String noFish() {
        if (Objects.equals(config.getString("no-record"), "none")) {
            return null;
        } else return getSTDPrefix() + config.getString("no-record");
    }

    public String noWinners() {
        return getSTDPrefix() + config.getString("no-winners");
    }

    public String getCompetitionEnd() {
        return getSTDPrefix() + config.getString("contest-end");
    }

    public String getCompetitionStart() {
        return getSTDPrefix() + config.getString("contest-start");
    }

    public int getLeaderboardCount() {
        return config.getInt("leaderboard-count");
    }

    public String getLargestFishLeaderboard() {
        if (config.getString("leaderboard-largest-fish") != null) {
            return getSTDPrefix() + config.getString("leaderboard-largest-fish");
        } else {
            if (config.getString("leaderboard") != null) {
                return getSTDPrefix() + config.getString("leaderboard");
            }
        }
        return "&r#{position} | {pos_colour}{player} &r({rarity} {fish}&r, {length}cm)";
    }

    public String getMostFishLeaderboard() {
        if (config.getString("leaderboard-most-fish") != null) {
            return getSTDPrefix() + config.getString("leaderboard-most-fish");
        } else {
            return "&r#{position} | {pos_colour}{player} &r({pos_colour}{amount} &rfish)";
        }
    }

    public String getEMFHelp() {
        return getSTDPrefix() + config.getString("help");
    }
    public String getBarSecond() {
        return config.getString("bossbar.second");
    }

    public String getBarMinute() {
        return config.getString("bossbar.minute");
    }

    public String getBarHour() {
        return config.getString("bossbar.hour");
    }

    public String getBarPrefix() {
        return config.getString("bossbar.prefix");
    }

    private String getPrefix() {
        return config.getString("prefix");
    }

    private String getStandardPrefixColour() {
        return config.getString("prefix-regular");
    }

    private String getAdminPrefixColour() {
        return config.getString("prefix-admin");
    }

    private String getErrorPrefixColour() {
        return config.getString("prefix-error");
    }

    public String getSTDPrefix() {
        return getStandardPrefixColour() + getPrefix() + "&r";
    }

    public String getAdminPrefix() {
        return getAdminPrefixColour() + getPrefix() + "&r";
    }

    public String getErrorPrefix() {
        return getErrorPrefixColour() + getPrefix() + "&r";
    }

    public String getReloaded() {
        return getAdminPrefix() + "successfully reloaded the plugin.";
    }

    public String getFishCaught() {
        return config.getString("fish-caught");
    }

    public String getNoPermission() {
        return getErrorPrefix() + config.getString("no-permission");
    }

    public String notInteger() {
        return getErrorPrefix() + "Please provide an integer video.";
    }

    public String competitionRunning() {
        return getErrorPrefix() + "There's already a competition running.";
    }

    public String competitionNotRunning() {
        return getErrorPrefix() + "There's no competition running right now.";
    }

    public String getNotEnoughPlayers() {
        return getErrorPrefix() + config.getString("not-enough-players");
    }

    public String getSellName() {
        return config.getString("sell-gui-name");
    }

    public String getConfirmName() {
        return config.getString("confirm-gui-name");
    }

    public String getNoValueName() {
        String s = config.getString("error-gui-name");
        if (s != null) return s;
        else return "&c&lCan't Sell";
    }

    public List<String> sellLore() {
        return config.getStringList("sell-gui-lore");
    }

    public List<String> noValueLore() {
        List<String> l = config.getStringList("error-gui-lore");
        if (!l.isEmpty()) return l;
        else {
            l.add("&c&lValue: &c$0");
            l.add("&cAdd your caught fish to this.");
            l.add("&cGUI to sell them.");
            return l;
        }
    }

    public String economyDisabled() {
        return getErrorPrefix() + "EvenMoreFish's economy features are disabled.";
    }

    public String fishCaughtBy() {
        String returning = config.getString("fish-caught-by");
        if (returning != null) return returning;
        else return "&fCaught by {player}";
    }

    public String fishLength() {
        String returning = config.getString("fish-length");
        if (returning != null) return returning;
        else return "&fMeasures {length}cm";
    }

    public String getRemainingWord() {
        String returning = config.getString("bossbar.remaining");
        if (returning != null) return returning;
        else return " left";
    }

    public String getRarityPrefix() {
        String returning = config.getString("fish-rarity-prefix");
        if (returning != null) return returning;
        else return "";
    }

    public void disabledInConsole() {
        Bukkit.getLogger().log(Level.SEVERE, "That command is disabled on the console, use it in-game instead.");
    }

    public String getNoCompPlaceholder() {
        String returning = config.getString("no-competition-running");
        if (returning != null) return returning;
        else return "No competition running right now.";
    }

    public String getNoPlayerInposPlaceholder() {
        String returning = config.getString("no-player-in-place");
        if (returning != null) return returning;
        else return "Start fishing to take this place";
    }

    public boolean shouldNullPlayerCompPlaceholder() {
        return config.getBoolean("emf-competition-player-null");
    }

    public boolean shouldNullSizeCompPlaceholder() {
        return config.getBoolean("emf-competition-size-null");
    }

    public boolean shouldNullFishCompPlaceholder() {
        return config.getBoolean("emf-competition-fish-null");
    }

    public String getFishFormat() {
        String returning = config.getString("emf-competition-fish-format");
        if (returning != null) return returning;
        else return "{length}cm &l{rarity} {fish}";
    }

    public String getPosColour(Integer position) {
        return "&d";
    }
}
