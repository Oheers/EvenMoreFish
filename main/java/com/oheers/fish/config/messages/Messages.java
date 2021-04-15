package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

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
        return getSTDPrefix() + config.getString("no-record");
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

    public String getLeaderboard() {
        return getSTDPrefix() + config.getString("leaderboard");
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

    public List<String> sellLore() {
        return config.getStringList("sell-gui-lore");
    }
}
