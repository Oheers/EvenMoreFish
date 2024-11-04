package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.exceptions.InvalidFishException;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class Names {

    public boolean regionCheck;
    // Gets all the fish names.
    Set<String> rarities, fishSet, fishList;
    YamlDocument fishConfiguration, rarityConfiguration;

    /*
     *  Goes through the fish branch of fish.yml, then for each rarity it realises on its journey,
     *  it goes down that branch looking for fish and their names. It then plops all this stuff into the
     *  main fish map. Badabing badaboom we've now populated our fish map.
     */
    public void loadRarities(YamlDocument fishConfiguration, YamlDocument rarityConfiguration) {
        fishList = new HashSet<>();

        // gets all the rarities - just their names, nothing else
        Section section = fishConfiguration.getSection("fish");
        if (section == null) {
            rarities = new HashSet<>();
        } else {
            rarities = section.getRoutesAsStrings(false);
        }

        for (String rarity : rarities) {

            this.fishConfiguration = fishConfiguration;
            this.rarityConfiguration = rarityConfiguration;

            // gets all the fish in said rarity, again - just their names
            Section raritySection = this.fishConfiguration.getSection("fish." + rarity);
            if (raritySection == null) {
                fishSet = new HashSet<>();
            } else {
                fishSet = this.fishConfiguration.getSection("fish." + rarity).getRoutesAsStrings(false);
            }
            fishList.addAll(fishSet);

            // creates a rarity object and a fish queue
            Rarity r = new Rarity(rarity, rarityColour(rarity), rarityWeight(rarity), rarityAnnounce(rarity), rarityUseConfigCasing(rarity), rarityOverridenLore(rarity));
            r.setPermission(rarityPermission(rarity));
            r.setDisplayName(rarityDisplayName(rarity));
            r.setRequirement(getRequirement(null, rarity, RaritiesFile.getInstance().getConfig()));

            List<Fish> fishQueue = new ArrayList<>();

            for (String fish : fishSet) {
                Fish canvas = null;

                // for each fish name, a fish object is made that contains the information gathered from that name
                try {
                    canvas = new Fish(r, fish);
                } catch (InvalidFishException exception) {
                    EvenMoreFish.getInstance().getLogger().log(Level.WARNING, exception.getMessage(), exception);
                    continue;
                }

                canvas.setRequirement(getRequirement(fish, rarity, FishFile.getInstance().getConfig()));
                weightCheck(canvas, fish, r, rarity);
                fishQueue.add(canvas);

                if (compCheckExempt(fish, rarity)) {
                    r.setHasCompExemptFish(true);
                    canvas.setCompExemptFish(true);
                    EvenMoreFish.getInstance().setRaritiesCompCheckExempt(true);
                }
            }

            // puts the collection of fish and their rarities into the main class
            EvenMoreFish.getInstance().getFishCollection().put(r, fishQueue);

            // memory saving or something
            fishList.clear();
        }
    }

    public void loadBaits(YamlDocument baitConfiguration) {
        Section section = baitConfiguration.getSection("baits");
        if (section == null) return;

        for (String s : section.getRoutesAsStrings(false)) {
            Bait bait = new Bait(s);

            List<String> rarityList;

            if (!(rarityList = baitConfiguration.getStringList("baits." + s + ".rarities")).isEmpty()) {
                for (String rarityString : rarityList) {
                    boolean foundRarity = false;
                    for (Rarity r : EvenMoreFish.getInstance().getFishCollection().keySet()) {
                        if (r.getValue().equalsIgnoreCase(rarityString)) {
                            bait.addRarity(r);
                            foundRarity = true;
                            break;
                        }
                    }
                    if (!foundRarity)
                        EvenMoreFish.getInstance().getLogger().severe(rarityString + " is not a loaded rarity value. It was not added to the " + s + " bait.");
                }
            }

            if (baitConfiguration.getSection("baits." + s + ".fish") != null) {
                for (String rarityString : baitConfiguration.getSection("baits." + s + ".fish").getRoutesAsStrings(false)) {
                    Rarity rarity = null;
                    for (Rarity r : EvenMoreFish.getInstance().getFishCollection().keySet()) {
                        if (r.getValue().equalsIgnoreCase(rarityString)) {
                            rarity = r;
                            break;
                        }
                    }

                    if (rarity == null) {
                        EvenMoreFish.getInstance().getLogger().severe(rarityString + " is not a loaded rarity value. It was not added to the " + s + " bait.");
                    } else {
                        for (String fishString : baitConfiguration.getStringList("baits." + s + ".fish." + rarityString)) {
                            boolean foundFish = false;
                            for (Fish f : EvenMoreFish.getInstance().getFishCollection().get(rarity)) {
                                if (f.getName().equalsIgnoreCase(fishString)) {
                                    bait.addFish(f);
                                    foundFish = true;
                                    break;
                                }
                            }
                            if (!foundFish)
                                EvenMoreFish.getInstance().getLogger().severe(fishString + " could not be found in the " + rarity.getValue() + " config. It was not added to the " + s + " bait.");
                        }
                    }
                }
            }

            EvenMoreFish.getInstance().getBaits().put(s, bait);
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

    private boolean rarityUseConfigCasing(String rarity) {
        return this.rarityConfiguration.getBoolean("rarities." + rarity + ".use-this-casing");
    }

    private String rarityDisplayName(String rarity) {
        return this.rarityConfiguration.getString("rarities." + rarity + ".displayname");
    }

    private String rarityPermission(String rarity) {
        return this.rarityConfiguration.getString("rarities." + rarity + ".permission");
    }

    private Requirement getRequirement(String name, String rarity, YamlDocument config) {
        Section requirementSection;
        if (name != null) {
            requirementSection = this.fishConfiguration.getSection("fish." + rarity + "." + name + ".requirements");
        } else {
            requirementSection = this.rarityConfiguration.getSection("rarities." + rarity + ".requirements");
        }

        Requirement requirement = new Requirement();
        if (requirementSection != null) {
            for (String requirementString : requirementSection.getRoutesAsStrings(false)) {
                List<String> values = new ArrayList<>();
                String fullPath = requirementSection.getRouteAsString() + "." + requirementString;
                if (config.isList(fullPath)) {
                    values.addAll(config.getStringList(fullPath));
                } else {
                    values.add(config.getString(fullPath));
                }
                requirement.add(requirementString, values);
            }
        }

        return requirement;
    }

    private void weightCheck(Fish fishObject, String name, Rarity rarityObject, String rarity) {
        if (this.fishConfiguration.getDouble("fish." + rarity + "." + name + ".weight") != 0) {
            rarityObject.setFishWeighted(true);
            fishObject.setWeight(this.fishConfiguration.getDouble("fish." + rarity + "." + name + ".weight"));
        }
    }

    private boolean compCheckExempt(String name, String rarity) {
        return this.fishConfiguration.getBoolean("fish." + rarity + "." + name + ".comp-check-exempt");
    }

    private int getDay(String name) {
        return this.fishConfiguration.getInt("fish.Christmas 2022." + name + ".day");
    }

}
