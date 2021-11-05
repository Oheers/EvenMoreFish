package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CompetitionFile {

    private final EvenMoreFish plugin;
    private FileConfiguration fishConfig;

    public CompetitionFile(EvenMoreFish plugin) {
        this.plugin = plugin;
        reload();
    }

    // Makes sure all th
    public void reload() {

        File fishFile = new File(this.plugin.getDataFolder(), "competitions.yml");

        if (!fishFile.exists()) {
            fishFile.getParentFile().mkdirs();
            this.plugin.saveResource("competitions.yml", false);
        }

        this.fishConfig = new YamlConfiguration();

        try {
            this.fishConfig.load(fishFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }

        EvenMoreFish.competitionFile = this;
    }

    public FileConfiguration getConfig() {
        if (this.fishConfig == null) reload();
        return this.fishConfig;
    }

}
