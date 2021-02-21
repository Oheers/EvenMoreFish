package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class FishFile {

    private EvenMoreFish plugin;
    private File fishFile;
    private FileConfiguration fishConfig;

    public FishFile(EvenMoreFish plugin) {
        this.plugin = plugin;
        saveDefault();
    }

    // Makes sure all th
    public void reload() {

        if (this.fishFile == null) this.fishFile = new File(this.plugin.getDataFolder(), "fish.yml");

        this.fishConfig = YamlConfiguration.loadConfiguration(this.fishFile);

        // Getting values from the fish.yml file
        InputStream stream = this.plugin.getResource("fish.yml");
        if (stream != null) {
            // Loads the input stream
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            this.fishConfig.setDefaults(defaultConfig);
        }
    }
    
    public void saveConfig() {
        if (this.fishConfig == null || this.fishFile == null) return;

        try {
            this.getConfig().save(this.fishFile);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "EvenMoreFish could not save the fish configuration in " + this.fishFile, e);
        }
    }

    public FileConfiguration getConfig() {
        if (this.fishConfig == null) reload();
        return this.fishConfig;
    }

    // Initialises config file
    public void saveDefault() {
        if (this.fishFile == null) this.fishFile = new File(this.plugin.getDataFolder(), "fish.yml");

        if (!this.fishFile.exists()) this.plugin.saveResource("fish.yml", false);
    }
}
