package com.oheers.fish.config.messages;

import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class Messages extends ConfigBase {

    private static Messages instance = null;
    private String localeFileName = null;

    public Messages() {
        super("messages.yml");
        instance = this;
    }

    public static Messages getInstance() {
        return instance;
    }

    @Override
    public File loadFile(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File configFile = new File(directory, getFileName());
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
                getPlugin().getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
            if (localeFileName == null) {
                localeFileName = "messages_" + MainConfig.getInstance().getLocale() + ".yml";
            }
            InputStream stream = getPlugin().getResource("locales/" + localeFileName);
            if (stream == null) {
                getPlugin().getLogger().log(Level.SEVERE, "Could not retrieve " + localeFileName);
                return null;
            }
            try {
                Files.copy(stream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                getPlugin().getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return configFile;
    }

    public String getSTDPrefix() {
        return getConfig().getString("prefix-regular") + getConfig().getString("prefix") + "&r";
    }
}
