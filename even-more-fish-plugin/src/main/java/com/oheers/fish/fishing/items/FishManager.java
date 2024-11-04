package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.exceptions.InvalidFishException;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class FishManager {

    private static FishManager instance;

    private Map<Rarity, List<Fish>> rarityMap;
    private boolean loaded = false;

    private FishManager() {
        rarityMap = new HashMap<>();
    }

    public static FishManager getInstance() {
        if (instance == null) {
            instance = new FishManager();
        }
        return instance;
    }

    public void load() {
        if (isLoaded()) {
            return;
        }
        loadRarities();
        logLoadedItems();
        loaded = true;
    }

    public void reload() {
        if (!isLoaded()) {
            return;
        }
        rarityMap.clear();
        loadRarities();
        logLoadedItems();
    }

    public void unload() {
        if (!isLoaded()) {
            return;
        }
        rarityMap.clear();
        loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }
    
    // Getters for config files
    
    public YamlDocument getFishConfiguration() {
        return FishFile.getInstance().getConfig();
    }
    
    public YamlDocument getRarityConfiguration() {
        return RaritiesFile.getInstance().getConfig();
    }

    // Getters for Rarities and Fish

    public @Nullable Rarity getRarity(@NotNull String rarityName) {
        for (Rarity rarity : rarityMap.keySet()) {
            if (rarity.getValue().equalsIgnoreCase(rarityName)) {
                return rarity;
            }
        }
        return null;
    }

    public List<Fish> getFishForRarity(@NotNull String rarityName) {
        return getFishForRarity(getRarity(rarityName));
    }

    public List<Fish> getFishForRarity(@Nullable Rarity rarity) {
        if (rarity == null) {
            return List.of();
        }
        return rarityMap.get(rarity);
    }

    public @Nullable Fish getFish(@NotNull String rarityName, @NotNull String fishName) {
        return getFish(getRarity(rarityName), fishName);
    }

    public @Nullable Fish getFish(@Nullable Rarity rarity, @NotNull String fishName) {
        if (rarity == null) {
            return null;
        }
        List<Fish> fishList = getFishForRarity(rarity);
        for (Fish fish : fishList) {
            if (fish.getName().equalsIgnoreCase(fishName)) {
                return fish;
            }
        }
        return null;
    }

    public Map<Rarity, List<Fish>> getRarityMap() {
        return rarityMap;
    }

    // Loading things

    private void logLoadedItems() {
        int allFish = 0;
        for (List<Fish> fishList : rarityMap.values()) {
            allFish += fishList.size();
        }
        EvenMoreFish.getInstance().getLogger().info("Loaded FishManager with " + rarityMap.size() + " Rarities and " + allFish + " Fish.");
    }

    private void loadRarities() {

        Section raritiesSection = getRarityConfiguration().getSection("rarities");

        if (raritiesSection == null) {
            return;
        }

        List<Rarity> rarityList = new ArrayList<>();

        // Collect the rarities
        raritiesSection.getRoutesAsStrings(false).forEach(rarityString -> {
            Section raritySection = raritiesSection.getSection(rarityString);
            if (raritySection == null) {
                raritySection = raritiesSection.createSection(rarityString);
            }
            rarityList.add(new Rarity(raritySection));
        });

        // Collect the fish in each rarity
        Section parentFishSection = getFishConfiguration().getSection("fish");
        rarityList.forEach(rarity -> {
            String rarityStr = rarity.getValue();
            Section raritySection = parentFishSection.getSection(rarityStr);
            if (raritySection == null) {
                return;
            }
            List<Fish> rarityFish = new ArrayList<>();
            raritySection.getRoutesAsStrings(false).forEach(fishStr -> {
                Section fishSection = raritySection.getSection(fishStr);
                if (fishSection == null) {
                    fishSection = raritySection.createSection(fishStr);
                }
                try {
                    rarityFish.add(new Fish(rarity, fishSection));
                } catch (InvalidFishException exception) {
                    EvenMoreFish.getInstance().getLogger().log(Level.WARNING, exception.getMessage(), exception);
                }
            });
            // Add the rarity and fish list to the map.
            rarityMap.put(rarity, rarityFish);
        });

    }

}
