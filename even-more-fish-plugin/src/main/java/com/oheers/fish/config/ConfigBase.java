package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class ConfigBase {

    private final String fileName;
    private final EvenMoreFish plugin;

    private FileConfiguration config = null;
    private File file = null;

    public ConfigBase(String fileName) {
        this.fileName = fileName;
        this.plugin = EvenMoreFish.getInstance();
        reload();
    }

    public void reload() {
        File configFile = loadFile(this.plugin.getDataFolder());
        if (configFile == null) {
            return;
        }

        FileConfiguration config = new YamlConfiguration();

        try {
            config.load(configFile);
            this.config = config;
            this.file = configFile;
        } catch (IOException | InvalidConfigurationException ex) {
            plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public File loadFile(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File configFile = new File(directory, fileName);
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }

            InputStream stream = plugin.getResource(fileName);
            if (stream == null) {
                plugin.getLogger().log(Level.SEVERE, "Could not retrieve " + fileName);
                return null;
            }
            try {
                Files.copy(stream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return configFile;
    }

    public FileConfiguration getConfig() { return this.config; }

    public File getFile() { return file; }

    public JavaPlugin getPlugin() { return this.plugin; }

    public String getFileName() { return this.fileName; }

    public void updateConfig() {
        File tempDirectory = new File(this.plugin.getDataFolder(), "temp");
        File tempConfigFile = loadFile(tempDirectory);
        if (tempConfigFile == null) {
            return;
        }

        FileConfiguration tempConfig = new YamlConfiguration();

        try {
            tempConfig.load(tempConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            return;
        }

        config.getKeys(true).forEach(key -> {
            if (!config.isConfigurationSection(key)) {
                tempConfig.set(key, config.get(key));
            }
            tempConfig.setComments(key, config.getComments(key));
        });
        try {
            tempConfig.save(file);
            tempConfigFile.delete();
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
        reload();
    }

}
