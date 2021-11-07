package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class MessageFile {

    private final EvenMoreFish plugin;
    private FileConfiguration messageConfig;

    public MessageFile(EvenMoreFish plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {

        File messageFile = getFile();

        if (!messageFile.exists()) {
            File parentFile = messageFile.getAbsoluteFile().getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            try {
                messageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream stream = this.plugin.getResource("locales" + File.separator + "messages_" + EvenMoreFish.mainConfig.getLocale() + ".yml");
            if (stream == null) {
                stream = this.plugin.getResource("locales" + File.separator + "messages_en.yml");
            }
            if (stream == null) {
                EvenMoreFish.logger.log(Level.SEVERE, "Could not get resource for EvenMoreFish/messages.yml");
                return;
            }
            try {
                Files.copy(stream, messageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.messageConfig = new YamlConfiguration();

        try {
            this.messageConfig.load(messageFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }

        EvenMoreFish.messageFile = this;
    }

    public FileConfiguration getConfig() {
        if (this.messageConfig == null) reload();
        return this.messageConfig;
    }

    public File getFile() {
        return new File(this.plugin.getDataFolder(), "messages.yml");
    }

    public void save() {
        this.messageConfig = new YamlConfiguration();
        try {
            this.messageConfig.save(getFile());
        } catch (IOException e) {
            EvenMoreFish.logger.log(Level.SEVERE, "Could not save EvenMoreFish/messages.yml");
            e.printStackTrace();
        }
    }

}
