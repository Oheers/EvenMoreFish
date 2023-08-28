package com.oheers.fish.config.impl;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BaitFile extends ConfigFile {
    public BaitFile(EvenMoreFish plugin) {
        super(plugin);
    }

    @Override
    public String getFileName() {
        return "baits.yml";
    }

    // Makes sure all th
    public void reload() {
        super.reload();
        EvenMoreFish.baitFile = this;
    }


    public List<String> getRodLoreFormat() {
        return config.getStringList("format.rod-lore");
    }

    public String getBaitFormat() {
        return config.getString("format.baits", "&6{amount} &e{bait}");
    }

    public int getMaxBaits() {
        return config.getInt("general.baits-per-rod", 7);
    }

    public boolean showUnusedBaitSlots() {
        return config.getBoolean("general.show-unused-slots", true);
    }

    public boolean doRodLore() {
        return config.getBoolean("general.add-to-lore", true);
    }

    public String unusedBaitSlotFormat() {
        return config.getString("format.unused-slot", "&7+ Available Slot");
    }

    public String getBaitTheme(String bait) {
        return config.getString("baits." + bait + ".bait-theme");
    }

    public List<String> getBaitLoreFormat() {
        return config.getStringList("format.bait-lore");
    }

    public String getBoostFishFormat() {
        return config.getString("format.boosts-fish");
    }

    public String getBoostRarityFormat() {
        return config.getString("format.boosts-rarity");
    }

    public String getBoostRaritiesFormat() {
        return config.getString("format.boosts-rarities");
    }

    public double getCatchWeight(String bait) {
        return config.getDouble("baits." + bait + ".catch-weight");
    }

    public double getApplicationWeight(String bait) {
        return config.getDouble("baits." + bait + ".application-weight");
    }

    public double getBoostRate() {
        return config.getDouble("general.boost", 1.0);
    }

    public boolean competitionsBlockBaits() {
        return config.getBoolean("general.competition-disable", true);
    }

    public double getBaitCatchPercentage() {
        return config.getDouble("general.catch-percentage");
    }

    public int getMaxBaitApplication(String baitName) {
        return config.getInt("baits." + baitName + ".max-baits", -1);
    }

    public String getDisplayName(String baitName) {
        return config.getString("baits." + baitName + ".item.displayname");
    }

    public List<String> getLore(String baitName) {
        return config.getStringList("baits." + baitName + ".lore");
    }

    public int getDropQuantity(String baitName) {
        return config.getInt("baits." + baitName + ".drop-quantity", 1);
    }
}
