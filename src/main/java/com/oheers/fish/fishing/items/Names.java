package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.baits.Bait;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Level;

public class Names {

    // Gets all the fish names.
    Set<String> rarities, fishSet, fishList;

    public boolean regionCheck;

    FileConfiguration fishConfiguration, rarityConfiguration;

    /*
     *  Goes through the fish branch of fish.yml, then for each rarity it realises on its journey,
     *  it goes down that branch looking for fish and their names. It then plops all this stuff into the
     *  main fish map. Badabing badaboom we've now populated our fish map.
     */
    public void loadRarities(FileConfiguration fishConfiguration, FileConfiguration rarityConfiguration) {
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

            List<Fish> fishQueue = new ArrayList<>();

            for (String fish : fishSet) {

                // for each fish name, a fish object is made that contains the information gathered from that name
                Fish canvas = new Fish(r, fish);
                canvas.setBiomes(getBiomes(fish, r.getValue()));
                canvas.setAllowedRegions(getRegions(fish, r.getValue()));
                canvas.setPermissionNode(permissionCheck(fish, rarity));
                weightCheck(canvas, fish, r, rarity);
                fishQueue.add(canvas);

                if (canvas.getAllowedRegions().size() > 0) regionCheck = true;

                if (compCheckExempt(fish, rarity)) {
                    r.setHasCompExemptFish(true);
                    canvas.setCompExemptFish(true);
                    EvenMoreFish.raritiesCompCheckExempt = true;
                }

            }

            // puts the collection of fish and their rarities into the main class
            EvenMoreFish.fishCollection.put(r, fishQueue);

            // memory saving or something
            fishList.clear();
        }
    }

    public void loadBaits(FileConfiguration baitConfiguration) {
        for (String s : baitConfiguration.getStringList("baits.")) {
            Bait bait = new Bait(s);

            List<String> itemList;

            if ((itemList = baitConfiguration.getStringList("baits. " + s + ".rarities")).size() != 0) {
                for (String rarityString : itemList) {
                    for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                        if (r.getValue().equalsIgnoreCase(rarityString)) {
                            bait.addRarity(r);
                            break;
                        }
                    }
                    EvenMoreFish.logger.log(Level.SEVERE, rarityString + " is not a loaded rarity value. It was not added to the " + s + " bait.");
                }
            } else if ((itemList = baitConfiguration.getStringList("baits. " + s + ".fish")).size() != 0) {
                for (String rarityString : itemList) {
                    Rarity rarity = null;
                    for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                        if (r.getValue().equalsIgnoreCase(rarityString)) {
                            rarity = r;
                            break;
                        }
                    }

                    if (rarity == null) {
                        EvenMoreFish.logger.log(Level.SEVERE, rarityString + " is not a loaded rarity value. It was not added to the " + s + " bait.");
                    } else {
                        for (String fishString : baitConfiguration.getStringList("baits. " + s + ".fish." + rarityString)) {
                            for (Fish f : EvenMoreFish.fishCollection.get(rarity)) {
                                if (f.getName().equalsIgnoreCase(fishString)) {
                                    bait.addFish(f);
                                    break;
                                }
                            }
                            EvenMoreFish.logger.log(Level.SEVERE, fishString + " is not a loaded fish value. It was not added to the " + s + " bait.");
                        }
                    }
                }
            }

            EvenMoreFish.baits.put(s, bait);
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

    private List<Biome> getBiomes(String name, String rarity) {
        // returns the biomes found in the "biomes:" section of the fish.yml
        List<Biome> biomes = new ArrayList<>();

        for (String biome : this.fishConfiguration.getStringList("fish." + rarity + "." + name + ".biomes")) {
            try {
                biomes.add(Biome.valueOf(biome));
            } catch (IllegalArgumentException iae) {
                EvenMoreFish.logger.log(Level.SEVERE, biome + " is not a valid biome, found when loading in: " + name);
            }
        }

        return biomes;
    }

    private List<String> getRegions(String name, String rarity) {
        // returns the regions found in the "allowed-regions:" section of the fish.yml
        return new ArrayList<>(this.fishConfiguration.getStringList("fish." + rarity + "." + name + ".allowed-regions"));
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

    private boolean compCheckExempt(String name, String rarity) {
        return this.fishConfiguration.getBoolean("fish." + rarity + "." + name + ".comp-check-exempt");
    }

}
