package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.BaitFile;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class BaitManager {
    
    private static BaitManager instance;

    private final TreeMap<String, Bait> baitMap;
    private boolean loaded;
    
    private BaitManager() {
        baitMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }
    
    public static BaitManager getInstance() {
        if (instance == null) {
            instance = new BaitManager();
        }
        return instance;
    }
    
    public void load() {
        if (isLoaded()) {
            return;
        }
        loadBaits();
        logLoadedItems();
        loaded = true;
    }
    
    public void reload() {
        if (!isLoaded()) {
            return;
        }
        baitMap.clear();
        loadBaits();
        logLoadedItems();
    }
    
    public void unload() {
        if (!isLoaded()) {
            return;
        }
        baitMap.clear();
        loaded = false;
    }
    
    public boolean isLoaded() {
        return loaded;
    }

    public Map<String, Bait> getBaitMap() {
        return Map.copyOf(baitMap);
    }

    public @Nullable Bait getBait(@Nullable String baitName) {
        if (baitName == null) {
            return null;
        }
        return baitMap.get(baitName);
    }

    public @Nullable Bait getBait(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        return getBait(BaitNBTManager.getBaitName(itemStack));
    }

    // Getters for config files

    public YamlDocument getBaitConfiguration() {
        return BaitFile.getInstance().getConfig();
    }

    // Loading things

    private void logLoadedItems() {
        EvenMoreFish.getInstance().getLogger().info("Loaded BaitManager with " + baitMap.size() + " Baits.");
    }

    private void loadBaits() {
        Section section = getBaitConfiguration().getSection("baits");
        if (section == null) {
            return;
        }
        for (String baitName : section.getRoutesAsStrings(false)) {
            Section baitSection = section.getSection(baitName);
            if (baitSection == null) {
                continue;
            }
            Bait bait = new Bait(baitSection);
            baitMap.put(baitName, bait);
        }
    }
    
}
