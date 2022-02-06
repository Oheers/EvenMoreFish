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
}
