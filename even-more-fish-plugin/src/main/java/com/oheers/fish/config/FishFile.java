package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import dev.dejvokep.boostedyaml.block.implementation.Section;

public class FishFile extends ConfigBase {

    private static FishFile instance = null;

    public FishFile() {
        super("fish.yml", "fish.yml", EvenMoreFish.getInstance(), false);
        instance = this;
        updateRequirementFormats();
    }

    private void updateRequirementFormats() {
        Section mainSection = getConfig().getSection("fish");
        if (mainSection == null) {
            return;
        }
        mainSection.getRoutesAsStrings(false).forEach(rarity -> {
            Section raritySection = getConfig().getSection("fish." + rarity);
            if (raritySection == null) {
                return;
            }
            raritySection.getRoutesAsStrings(false).forEach(fish -> {
                Section fishSection = getConfig().getSection("fish." + rarity + "." + fish);
                if (fishSection == null) {
                    return;
                }
                Section ingameSection = fishSection.getSection("requirements.ingame-time");
                if (ingameSection != null) {
                    int min = ingameSection.getInt("minTime");
                    int max = ingameSection.getInt("maxTime");
                    ingameSection.remove("minTime");
                    ingameSection.remove("maxTime");
                    getConfig().set("fish." + rarity + "." + fish + ".requirements.ingame-time", min + "-" + max);
                }
                Section irlSection = fishSection.getSection("requirements.irl-time");
                if (irlSection != null) {
                    String min = irlSection.getString("minTime");
                    String max = irlSection.getString("maxTime");
                    irlSection.remove("minTime");
                    irlSection.remove("maxTime");
                    getConfig().set("fish." + rarity + "." + fish + ".requirements.irl-time", min + "-" + max);
                }
            });
        });
        save();
    }

    public static FishFile getInstance() {
        return instance;
    }

}
