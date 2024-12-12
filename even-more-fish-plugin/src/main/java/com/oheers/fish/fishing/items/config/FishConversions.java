package com.oheers.fish.fishing.items.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.ConfigBase;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FishConversions extends RarityConversions {

    @Override
    public void performCheck() {
        File fishFile = new File(EvenMoreFish.getInstance().getDataFolder(), "fish.yml");
        if (!fishFile.exists() || !fishFile.isFile()) {
            return;
        }
        EvenMoreFish.getInstance().getLogger().info("Performing automatic conversion of fish configs.");
        File raritiesDir = getRaritiesDirectory();
        if (!raritiesDir.exists()) {
            // Do nothing if the rarities directory does not exist.
            return;
        }
        ConfigBase config = new ConfigBase(fishFile, EvenMoreFish.getInstance(), false);
        Section fishSection = config.getConfig().getSection("fish");
        if (fishSection == null) {
            finalizeConversion(config);
            return;
        }
        for (String rarityKey : fishSection.getRoutesAsStrings(false)) {
            Section section = fishSection.getSection(rarityKey);
            if (section != null) {
                convertSectionToFile(section);
            }
        }
        finalizeConversion(config);
    }

    private void finalizeConversion(@NotNull ConfigBase fishConfig) {
        // Rename the file to fish.yml.old
        File file = fishConfig.getFile();
        file.renameTo(new File(EvenMoreFish.getInstance().getDataFolder(), "fish.yml.old"));
        file.delete();
    }

    private void convertSectionToFile(@NotNull Section section) {
        String id = section.getNameAsString();
        if (id == null) {
            return;
        }
        File rarityFile = new File(EvenMoreFish.getInstance().getDataFolder(), "rarities/" + id + ".yml");
        // Do nothing if the file doesn't exist.
        if (!rarityFile.exists()) {
            return;
        }
        ConfigBase configBase = new ConfigBase(rarityFile, EvenMoreFish.getInstance(), false);
        Section fishSection = configBase.getConfig().createSection("fish");
        fishSection.setAll(section.getRouteMappedValues(true));
        configBase.save();
    }

}
