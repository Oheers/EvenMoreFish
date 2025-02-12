package com.oheers.fish.gui.guis.journal;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;

import java.io.File;
import java.io.IOException;

public class GUIConfig {

    private static GUIConfig instance;
    private final YamlDocument config;

    private GUIConfig(EmfCodex plugin) throws IOException {
        this.config = YamlDocument.create(new File(plugin.getDataFolder(), "gui.yml"), plugin.getResource("gui.yml"),
                LoaderSettings.builder().setAutoUpdate(false).build(), DumperSettings.DEFAULT);
    }

    public static void init(EmfCodex plugin) throws IOException {
        if (instance == null) {
            instance = new GUIConfig(plugin);
        }
    }

    public static GUIConfig getInstance() {
        return instance;
    }

    public YamlDocument getConfig() {
        return config;
    }
}