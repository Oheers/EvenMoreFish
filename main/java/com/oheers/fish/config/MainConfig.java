package com.oheers.fish.config;

import com.oheers.fish.FishUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

public class MainConfig {

    // returns default values found in config.yml version >= 6
    private Material defaultRewardMaterial(Integer position) {
        switch (position) {
            case 1: return Material.DIAMOND;
            case 2: return Material.GOLD_INGOT;
            case 3: return Material.IRON_INGOT;
            case 4: return Material.BRICK;
            default: return Material.STICK;
        }
    }

    private String defaultRewardTitle(Integer position) {
        switch (position) {
            case 1: return FishUtils.translateHexColorCodes("&b&lFirst Place (#1)");
            case 2: return FishUtils.translateHexColorCodes("&e&lSecond Place (#2)");
            case 3: return FishUtils.translateHexColorCodes("&#dddddd&lThird Place (#3)");
            case 4: return FishUtils.translateHexColorCodes("&#e68d5c&lFourth Place (#4)");
            case 5: return FishUtils.translateHexColorCodes("&#e68d5c&lFifth Place (#5)");
            default: return FishUtils.translateHexColorCodes("&#e68d5c&l(#" + position + ")");
        }
    }

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

    public Material getRewardGUIItem(Integer position) {
        String returning = config.getString("reward-gui.positions." + position + ".material");
        if (returning != null) return Material.valueOf(returning);
        else return defaultRewardMaterial(position);
    }

    public String getRewardGUITitle(Integer position) {
        String returning = config.getString("reward-gui.positions." + position + ".title");
        if (returning != null) return FishUtils.translateHexColorCodes(returning);
        else return defaultRewardTitle(position);
    }

    public String rewardEffect() {
        return config.getString("reward-gui.reward-effect");
    }

    public String rewardItem() {
        return config.getString("reward-gui.reward-item");
    }

    public String rewardMoney() {
        return config.getString("reward-gui.reward-money");
    }

    public String rewardHealth() {
        return config.getString("reward-gui.reward-health");
    }

    public String rewardHunger() {
        return config.getString("reward-gui.reward-hunger");
    }
}
