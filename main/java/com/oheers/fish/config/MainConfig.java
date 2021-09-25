package com.oheers.fish.config;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.logging.Level;

public class MainConfig {

    private FileConfiguration config = Bukkit.getPluginManager().getPlugin("EvenMoreFish").getConfig();

    public int configVersion() {
        return config.getInt("config-version");
    }

    public int getCompetitionDuration() {
        return config.getInt("competitions.duration");
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

    public boolean getEnabled() {
        return config.getBoolean("enabled");
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

    public String rewardCommand(String command) {
        return config.getString("reward-gui.command-override." + command);
    }

    public Material getSellAllMaterial() {
        String s = config.getString("gui.sell-all-item");
        if (s != null) {
            try {
                return Material.valueOf(s);
            } catch (IllegalArgumentException exception) {
                Bukkit.getLogger().log(Level.SEVERE, s + " is not a valid material type in config.yml gui.sell-all-item.");
            }
        }
        return Material.COD_BUCKET;
    }

    public Material getSellAllConfirmMaterial() {
        String s = config.getString("gui.sell-all-item-confirm");
        if (s != null) {
            try {
                return Material.valueOf(s);
            } catch (IllegalArgumentException exception) {
                Bukkit.getLogger().log(Level.SEVERE, s + " is not a valid material type in config.yml gui.sell-all-item-confirm.");
            }
        }
        return Material.TROPICAL_FISH_BUCKET;
    }

    public Material getSellAllErrorMaterial() {
        String s = config.getString("gui.sell-all-item-error");
        if (s != null) {
            try {
                return Material.valueOf(s);
            } catch (IllegalArgumentException exception) {
                Bukkit.getLogger().log(Level.SEVERE, s + " is not a valid material type in config.yml gui.sell-all-item-confirm.");
            }
        }
        return Material.SALMON_BUCKET;
    }

    public int getSellAllSlot() {
        int returning = config.getInt("gui.sell-all-slot");
        if (returning > 9 || returning < 1) return 6;
        else return returning;
    }

    public int getSellSlot() {
        int returning = config.getInt("gui.sell-slot");
        if (returning > 9 || returning < 1) return 4;
        else return returning;
    }
}
