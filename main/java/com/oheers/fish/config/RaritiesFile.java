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
        saveDefault();
    }

    public void reload() {

        if (this.raritiesFile == null) this.raritiesFile = new File(this.plugin.getDataFolder(), "rarities.yml");

        this.raritiesConfig = YamlConfiguration.loadConfiguration(this.raritiesFile);

        // Getting values from the fish.yml file
        InputStream stream = this.plugin.getResource("rarities.yml");
        if (stream != null) {
            // Loads the input stream
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            this.raritiesConfig.setDefaults(defaultConfig);
        }
    }
    
    public void saveConfig() {
        if (this.raritiesConfig == null || this.raritiesFile == null) return;

        try {
            this.getConfig().save(this.raritiesFile);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "EvenMoreFish could not save the rarity configuration in " + this.raritiesFile, e);
        }
    }

    public FileConfiguration getConfig() {
        if (this.raritiesConfig == null) reload();
        return this.raritiesConfig;
    }

    // Initialises config file
    public void saveDefault() {
        if (this.raritiesFile == null) this.raritiesFile = new File(this.plugin.getDataFolder(), "rarities.yml");

        if (!this.raritiesFile.exists()) this.plugin.saveResource("rarities.yml", false);
    }
}
