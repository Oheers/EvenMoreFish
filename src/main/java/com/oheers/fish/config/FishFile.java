package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FishFile {

    private final EvenMoreFish plugin;
    private FileConfiguration fishConfig;

    public FishFile(EvenMoreFish plugin) {
        this.plugin = plugin;
        reload();
    }

    // Makes sure all th
    public void reload() {

        File fishFile = new File(this.plugin.getDataFolder(), "fish.yml");

        if (!fishFile.exists()) {
            fishFile.getParentFile().mkdirs();
            this.plugin.saveResource("fish.yml", false);
        }

        this.fishConfig = new YamlConfiguration();

        try {
            this.fishConfig.load(fishFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }

        EvenMoreFish.fishFile = this;
    }

    public FileConfiguration getConfig() {
        if (this.fishConfig == null) reload();
        return this.fishConfig;
    }
}
