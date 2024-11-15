package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;

import java.util.List;

public class BaitFile extends ConfigBase {

    private static BaitFile instance = null;

    public BaitFile() {
        super("baits.yml", "baits.yml", EvenMoreFish.getInstance(), false);
        instance = this;
    }
    
    public static BaitFile getInstance() {
        return instance;
    }

    public List<String> getRodLoreFormat() {
        return getConfig().getStringList("format.rod-lore");
    }

    public String getBaitFormat() {
        return getConfig().getString("format.baits", "&6{amount} &e{bait}");
    }

    public int getMaxBaits() {
        return getConfig().getInt("general.baits-per-rod", 7);
    }

    public boolean showUnusedBaitSlots() {
        return getConfig().getBoolean("general.show-unused-slots", true);
    }

    public boolean doRodLore() {
        return getConfig().getBoolean("general.add-to-lore", true);
    }

    public String unusedBaitSlotFormat() {
        return getConfig().getString("format.unused-slot", "&7+ Available Slot");
    }

    public String getBaitTheme(String bait) {
        return getConfig().getString("baits." + bait + ".bait-theme");
    }

    public List<String> getBaitLoreFormat() {
        return getConfig().getStringList("format.bait-lore");
    }

    public String getBoostFishFormat() {
        return getConfig().getString("format.boosts-fish");
    }

    public String getBoostRarityFormat() {
        return getConfig().getString("format.boosts-rarity");
    }

    public String getBoostRaritiesFormat() {
        return getConfig().getString("format.boosts-rarities");
    }

    public double getCatchWeight(String bait) {
        return getConfig().getDouble("baits." + bait + ".catch-weight");
    }

    public double getApplicationWeight(String bait) {
        return getConfig().getDouble("baits." + bait + ".application-weight");
    }

    public double getBoostRate() {
        return getConfig().getDouble("general.boost", 1.0);
    }

    public boolean competitionsBlockBaits() {
        return getConfig().getBoolean("general.competition-disable", true);
    }

    public double getBaitCatchPercentage() {
        return getConfig().getDouble("general.catch-percentage");
    }

    public int getMaxBaitApplication(String baitName) {
        return getConfig().getInt("baits." + baitName + ".max-baits", -1);
    }

    public String getDisplayName(String baitName) {
        return getConfig().getString("baits." + baitName + ".item.displayname");
    }

    public List<String> getLore(String baitName) {
        return getConfig().getStringList("baits." + baitName + ".lore");
    }

    public int getDropQuantity(String baitName) {
        return getConfig().getInt("baits." + baitName + ".drop-quantity", 1);
    }

    public boolean isBaitInfinite(String baitName) {
        return getConfig().getBoolean("baits." + baitName + ".infinite", false);
    }
}
