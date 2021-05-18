package com.oheers.fish.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

public class MainConfig {

    private FileConfiguration config = Bukkit.getPluginManager().getPlugin("EvenMoreFish").getConfig();

    public int configVersion() {
        return config.getInt("config-version");
    }

    public int getCompetitionDuration() {
        return config.getInt("competitions.duration");
    }

    public List<String> getCompetitionTimes() {
        return config.getStringList("competitions.times");
    }

    public String getBossbarColour() {
        return config.getString("competitions.bossbar-colour");
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

    public int getMinimumPlayers() {
        int test = config.getInt("competitions.minimum-players");
        if (test != 0) {
            return test;
        } else return 5;
    }

    public boolean broadcastOnlyRods() {
        return config.getBoolean("broadcast-only-rods");
    }

    public boolean regionWhitelist() {
        return config.getStringList("allowed-regions").size() != 0;
    }

    public List<String> getAllowedRegions() {
        return config.getStringList("allowed-regions");
    }

    public boolean isEconomyEnabled() {
        return config.getBoolean("enable-economy");
    }

    public boolean isDaySpecific() {
        return config.getStringList("competitions.days").size() != 0;
    }

    public List<Integer> getActiveDays() {
        return config.getIntegerList("competitions.days");
    }

    public String getFiller() {
        String returning = config.getString("gui.filler");
        if (returning != null) return returning;
        else return "GRAY_STAINED_GLASS_PANE";
    }

    public String getFillerError() {
        String returning = config.getString("gui.filler-error");
        if (returning != null) return returning;
        else return "RED_STAINED_GLASS_PANE";
    }

    public String getSellItem() {
        String returning = config.getString("gui.sell-item");
        if (returning != null) return returning;
        else return "GOLD_INGOT";
    }

    public String getSellItemConfirm() {
        String returning = config.getString("gui.sell-item-confirm");
        if (returning != null) return returning;
        else return "GOLD_BLOCK";
    }

    public String getSellItemError() {
        String returning = config.getString("gui.sell-item-error");
        if (returning != null) return returning;
        else return "REDSTONE_BLOCK";
    }

    public Integer getGUISize() {
        int returning = config.getInt("gui.size");
        if (returning <= 0 || returning > 5) return 3;
        else return returning;
    }

    public boolean sellOverDrop() {
        return config.getBoolean("gui.sell-over-drop");
    }

    public boolean disableMcMMOTreasure() {
        return config.getBoolean("disable-mcmmo-loot");
    }
}
