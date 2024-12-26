package com.oheers.fish.fishing.items.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.ConfigBase;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class RarityConversions {

    public void performCheck() {
        File raritiesFile = new File(EvenMoreFish.getInstance().getDataFolder(), "rarities.yml");
        if (!raritiesFile.exists() || !raritiesFile.isFile()) {
            return;
        }
        EvenMoreFish.getInstance().getLogger().info("Performing automatic conversion of rarity configs.");
        File raritiesDir = getRaritiesDirectory();
        if (!raritiesDir.exists()) {
            raritiesDir.mkdirs();
        }
        ConfigBase config = new ConfigBase(raritiesFile, EvenMoreFish.getInstance(), false);
        Section raritiesSection = config.getConfig().getSection("rarities");
        if (raritiesSection == null) {
            finalizeConversion(config);
            return;
        }
        for (String rarityKey : raritiesSection.getRoutesAsStrings(false)) {
            Section section = raritiesSection.getSection(rarityKey);
            if (section != null) {
                convertSectionToFile(section);
            }
        }
        finalizeConversion(config);
    }

    private void finalizeConversion(@NotNull ConfigBase raritiesConfig) {
        // Rename the file to rarities.yml.old
        File file = raritiesConfig.getFile();
        file.renameTo(new File(EvenMoreFish.getInstance().getDataFolder(), "rarities.yml.old"));
        file.delete();

        EvenMoreFish.getInstance().getLogger().severe("Your rarity configs have been automatically converted to the new format.");
        EvenMoreFish.getInstance().getLogger().severe("You may want to disable all rarities in the plugins/EvenMoreFish/rarities/defaults folder.");
    }

    /**
     * @return The 'rarities' directory. This may not exist yet.
     */
    public File getRaritiesDirectory() {
        return new File(EvenMoreFish.getInstance().getDataFolder(), "rarities");
    }

    private void convertSectionToFile(@NotNull Section section) {
        String id = section.getNameAsString();
        if (id == null) {
            return;
        }
        File file = new File(EvenMoreFish.getInstance().getDataFolder(), "rarities/" + id + ".yml");
        ConfigBase configBase = new ConfigBase(file, EvenMoreFish.getInstance(), false);
        YamlDocument config = configBase.getConfig();
        config.setAll(section.getRouteMappedValues(true));
        config.set("id", id);
        configBase.save();
    }

}
