package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.c2021.c2021Event;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Names {

    // Gets all the fish names.
    Set<String> rarities, fishSet, fishList;

    FileConfiguration fishConfiguration, rarityConfiguration;

    /*
     *  Goes through the fish branch of fish.yml, then for each rarity it realises on its journey,
     *  it goes down that branch looking for fish and their names. It then plops all this stuff into the
     *  main fish map. Badabing badaboom we've now populated our fish map.
     */
    public void loadRarities(FileConfiguration fishConfiguration, FileConfiguration rarityConfiguration, boolean isC2021) {
        this.fishConfiguration = fishConfiguration;
        this.rarityConfiguration = rarityConfiguration;

        fishList = new HashSet<>();

        // gets all the rarities - just their names, nothing else
        rarities = this.fishConfiguration.getConfigurationSection("fish").getKeys(false);

        for (String rarity : rarities) {

            // gets all the fish in said rarity, again - just their names
            fishSet = this.fishConfiguration.getConfigurationSection("fish." + rarity).getKeys(false);
            fishList.addAll(fishSet);

            // creates a rarity object and a fish queue
            Rarity r = new Rarity(rarity, rarityColour(rarity), rarityWeight(rarity), rarityAnnounce(rarity), rarityOverridenLore(rarity));
            r.setPermission(rarityPermission(rarity));
            r.setDisplayName(rarityDisplayName(rarity));
            r.setC2021(isC2021);

            List<Fish> fishQueue = new ArrayList<>();

            for (String fish : fishSet) {

                // for each fish name, a fish object is made that contains the information gathered from that name
                Fish canvas = new Fish(r, fish);
                canvas.setConfigurationFile(fishConfiguration);
                canvas.setBiomes(getBiomes(fish, r.getValue()));
                canvas.setGlowing(getGlowing(fish, r.getValue()));
                canvas.setPermissionNode(permissionCheck(fish, rarity));
                weightCheck(canvas, fish, r, rarity);
                fishQueue.add(canvas);

                if (isC2021) c2021Check(canvas);

            }

            // puts the collection of fish and their rarities into the main class
            EvenMoreFish.fishCollection.put(r, fishQueue);

            // memory saving or something
            fishList.clear();
        }
    }

    private String rarityColour(String rarity) {
        String colour = this.rarityConfiguration.getString("rarities." + rarity + ".colour");
        if (colour == null) return "&f";
        return colour;
    }

    private double rarityWeight(String rarity) {
        return this.rarityConfiguration.getDouble("rarities." + rarity + ".weight");
    }

    private boolean rarityAnnounce(String rarity) {
        return this.rarityConfiguration.getBoolean("rarities." + rarity + ".broadcast");
    }

    private String rarityOverridenLore(String rarity) {
        return this.rarityConfiguration.getString("rarities." + rarity + ".override-lore");
    }

    private String rarityDisplayName(String rarity) {
        return this.rarityConfiguration.getString("rarities." + rarity + ".displayname");
    }

    private String rarityPermission(String rarity) {
        return this.rarityConfiguration.getString("rarities." + rarity + ".permission");
    }

    private void c2021Check(Fish f) {
        if (this.fishConfiguration.getInt("fish." + f.getRarity().getValue() + "." + f.getName() + ".day") != 0) {
            c2021Event.setRegisteredFish(f, this.fishConfiguration.getInt("fish." + f.getRarity().getValue() + "." + f.getName() + ".day"));
        }
    }

    private List<Biome> getBiomes(String name, String rarity) {
        // returns the biomes found in the "biomes:" section of the fish.yml
        List<Biome> biomes = new ArrayList<>();

        for (String biome : this.fishConfiguration.getStringList("fish." + rarity + "." + name + ".biomes")) {
            biomes.add(Biome.valueOf(biome));
        }

        return biomes;
    }

    private void weightCheck(Fish fishObject, String name, Rarity rarityObject, String rarity) {
        if (this.fishConfiguration.getDouble("fish." + rarity + "." + name + ".weight") != 0) {
            rarityObject.setFishWeighted(true);
            fishObject.setWeight(this.fishConfiguration.getDouble("fish." + rarity + "." + name + ".weight"));
        }
    }

    private String permissionCheck(String name, String rarity) {
        return this.fishConfiguration.getString("fish." + rarity + "." + name + ".permission");
    }

    private boolean getGlowing(String name, String rarity) {
        return this.fishConfiguration.getBoolean("fish." + rarity + "." + name + ".glowing");
    }

}
