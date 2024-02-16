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
        File configFile = new File(this.plugin.getDataFolder(), fileName);
        if (!configFile.exists()) {
            File parentFile = configFile.getAbsoluteFile().getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }

            InputStream stream = plugin.getResource(fileName);
            if (stream == null) {
                plugin.getLogger().log(Level.SEVERE, "Could not retrieve " + fileName);
                return;
            }
            try {
                Files.copy(stream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
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

    public FileConfiguration getConfig() { return this.config; }

    public File getFile() { return file; }

}
