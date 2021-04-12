package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class RaritiesFile {

    private EvenMoreFish plugin;
    private File raritiesFile;
    private FileConfiguration raritiesConfig;

    public RaritiesFile(EvenMoreFish plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {

        this.raritiesFile = new File(this.plugin.getDataFolder(), "rarities.yml");

        if (this.raritiesFile.exists()) {
            this.raritiesFile.getParentFile().mkdirs();
            this.plugin.saveResource("rarities.yml", false);
        }

        this.raritiesConfig = new YamlConfiguration();

        try {
            this.raritiesConfig.load(this.raritiesFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }

        EvenMoreFish.raritiesFile = this;
    }

    public FileConfiguration getConfig() {
        if (this.raritiesConfig == null) reload();
        return this.raritiesConfig;
    }
}
