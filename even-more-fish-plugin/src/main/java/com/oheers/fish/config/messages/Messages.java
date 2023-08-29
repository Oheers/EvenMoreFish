package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class Messages extends ConfigFile {
    @Override
    public String getFileName() {
        return "messages.yml";
    }

    public Messages(EvenMoreFish plugin) {
        super(plugin);
    }

    public void reload() {
        File messageFile = new File(this.plugin.getDataFolder(), "messages.yml");

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

            InputStream stream = this.plugin.getResource("locales/messages_" + EvenMoreFish.mainConfig.getLocale() + ".yml");
            if (stream == null) {
                stream = this.plugin.getResource("locales/messages_en.yml");
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

        this.config = new YamlConfiguration();

        try {
            this.config.load(messageFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public int configVersion() {
        return config.getInt("config-version");
    }

    public String getSTDPrefix() {
        return config.getString("prefix-regular") + config.getString("prefix") + "&r";
    }
}
