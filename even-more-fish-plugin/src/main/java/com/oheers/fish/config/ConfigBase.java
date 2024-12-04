package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.Settings;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class ConfigBase {

    private final String fileName;
    private final String resourceName;
    private final Plugin plugin;
    private final boolean configUpdater;

    private YamlDocument config = null;
    private File file = null;

    public ConfigBase(@NotNull File file, @NotNull Plugin plugin, boolean configUpdater) {
        this.fileName = file.getName();
        this.resourceName = null;
        this.plugin = plugin;
        this.configUpdater = configUpdater;
        reload(file);
        update();
    }

    public ConfigBase(@NotNull String fileName, @NotNull String resourceName, @NotNull Plugin plugin, boolean configUpdater) {
        this.fileName = fileName;
        this.resourceName = resourceName;
        this.plugin = plugin;
        this.configUpdater = configUpdater;
        reload(new File(getPlugin().getDataFolder(), getFileName()));
        update();
    }

    public void reload(@NotNull File configFile) {
        List<Settings> settingsList = new ArrayList<>(Arrays.asList(
                getGeneralSettings(),
                getDumperSettings(),
                getLoaderSettings()
        ));

        if (configUpdater) {
            settingsList.add(getUpdaterSettings());
        }

        final Settings[] settings = settingsList.toArray(new Settings[0]);

        try {
            InputStream resource = getResourceName() == null ? null : getPlugin().getResource(getResourceName());
            if (resource == null) {
                this.config = YamlDocument.create(configFile, settings);
            } else {
                this.config = YamlDocument.create(configFile, resource, settings);
            }
            this.file = configFile;
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void reload() {
        reload(this.file);
    }

    public final YamlDocument getConfig() {
        if (this.config == null) {
            throw new RuntimeException(getFileName() + " has not loaded properly. Please check for startup errors.");
        }
        return this.config;
    }

    public final File getFile() { return this.file; }

    public final Plugin getPlugin() { return this.plugin; }

    public final String getFileName() { return this.fileName; }

    public final String getResourceName() { return this.resourceName; }

    public GeneralSettings getGeneralSettings() {
        return GeneralSettings.builder().setUseDefaults(false).build();
    }

    public DumperSettings getDumperSettings() {
        return DumperSettings.DEFAULT;
    }

    public LoaderSettings getLoaderSettings() {
        return LoaderSettings.DEFAULT;
    }

    public UpdaterSettings getUpdaterSettings() {
        return UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).setKeepAll(true).build();
    }

    public void save() {
        try {
            getConfig().save();
        } catch (IOException exception) {
            EvenMoreFish.getInstance().getLogger().warning("Failed to save " + getFileName());
        }
    }

    public void update() {
        try {
            getConfig().update();
        } catch (IOException exception) {
            EvenMoreFish.getInstance().getLogger().warning("Failed to update " + getFileName());
        }
    }

}
