package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

public class GUIConfig extends ConfigBase {

    private static GUIConfig instance = null;

    public GUIConfig() {
        super("guis.yml", "guis.yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }
    
    public static GUIConfig getInstance() {
        return instance;
    }

    @Override
    public UpdaterSettings getUpdaterSettings() {
        UpdaterSettings.Builder builder = UpdaterSettings.builder(super.getUpdaterSettings());

        // Config Version 5 - Remove competition menu button
        builder.addCustomLogic("5", document -> document.remove("main-menu.coming-soon-competitions"));

        // Config Version 6 - Remove journal coming soon button
        builder.addCustomLogic("6", document -> document.remove("main-menu.coming-soon-journal"));

        return builder.build();
    }

}
