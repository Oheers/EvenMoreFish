package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BaitFile {

	private final EvenMoreFish plugin;
	private FileConfiguration baitConfig;

	public BaitFile(EvenMoreFish plugin) {
		this.plugin = plugin;
		reload();
	}

	// Makes sure all th
	public void reload() {

		File baitFile = new File(this.plugin.getDataFolder(), "baits.yml");

		if (!baitFile.exists()) {
			baitFile.getParentFile().mkdirs();
			this.plugin.saveResource("baits.yml", false);
		}

		this.baitConfig = new YamlConfiguration();

		try {
			this.baitConfig.load(baitFile);
		} catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
			e.printStackTrace();
		}

		EvenMoreFish.baitFile = this;
	}

	public FileConfiguration getConfig() {
		if (this.baitConfig == null) reload();
		return this.baitConfig;
	}

	public List<String> getRodLoreFormat() {
		return baitConfig.getStringList("format.rod-lore");
	}

	public String getBaitFormat() {
		return baitConfig.getString("format.baits", "&6{amount} &e{bait}");
	}

	public int getMaxBaits() {
		return baitConfig.getInt("general.baits-per-rod", 7);
	}

	public boolean showUnusedBaitSlots() {
		return baitConfig.getBoolean("general.show-unused-slots", true);
	}

	public boolean doRodLore() {
		return baitConfig.getBoolean("general.add-to-lore", true);
	}

	public String unusedBaitSlotFormat() {
		return baitConfig.getString("format.unused-slot", "&7+ Available Slot");
	}

	public String getBaitTheme(String bait) {
		return baitConfig.getString("baits." + bait + ".bait-theme");
	}

	public List<String> getBaitLoreFormat() {
		return baitConfig.getStringList("format.bait-lore");
	}

	public String getBoostFishFormat() {
		return baitConfig.getString("format.boosts-fish");
	}

	public String getBoostRarityFormat() {
		return baitConfig.getString("format.boosts-rarity");
	}

	public String getBoostRaritiesFormat() {
		return baitConfig.getString("format.boosts-rarities");
	}

	public double getCatchWeight(String bait) {
		return baitConfig.getDouble("baits." + bait + ".catch-weight");
	}

	public double getApplicationWeight(String bait) {
		return baitConfig.getDouble("baits." + bait + ".application-weight");
	}

	public double getBoostRate() {
		return baitConfig.getDouble("general.boost", 1.0);
	}

	public boolean competitionsBlockBaits() {
		return baitConfig.getBoolean("general.competition-disable", true);
	}

	public double getBaitCatchPercentage() {
		return baitConfig.getDouble("general.catch-percentage");
	}

	public int getMaxBaitApplication(String baitName) {
		return baitConfig.getInt("baits." + baitName + ".max-baits", -1);
	}

	public String getDisplayName(String baitName) {
		return baitConfig.getString("baits." + baitName + ".item.displayname");
	}
}
