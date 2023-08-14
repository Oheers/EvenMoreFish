package com.oheers.fish.fishing.items;

import com.oheers.fish.FishUtils;
import com.oheers.fish.requirements.Requirement;

import java.util.ArrayList;
import java.util.List;

public class Rarity {

    public String overridenLore;
    public String permission;
    String value, colour;
    double weight;
    boolean announce;
    boolean fishWeighted;
    boolean hasCompExemptFish;
    String displayName;
    List<Requirement> requirements = new ArrayList<>();

    public Rarity(String value, String colour, double weight, boolean announce, String overridenLore) {
        this.value = value;
        this.colour = colour;
        this.weight = weight;
        this.announce = announce;
        this.overridenLore = overridenLore;
    }

    public String getValue() {
        return this.value;
    }

    public String getColour() {
        return this.colour;
    }

    public double getWeight() {
        return this.weight;
    }

    public boolean getAnnounce() {
        return this.announce;
    }

    public boolean isFishWeighted() {
        return fishWeighted;
    }

    public void setFishWeighted(boolean fishWeighted) {
        this.fishWeighted = fishWeighted;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /**
     * Adds a requirement needed for players to be able to catch a fish. If all rarities have unmet requirements the plugin
     * will likely break and give no fish. This will also modify the weight values as it will no longer be in the pool.
     *
     * @param requirement The requirement that needs to be met in order to catch a fish of this rarity.
     */
    public void addRequirement(Requirement requirement) {
        this.requirements.add(requirement);
    }

    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    public String getLorePrep() {
        if (overridenLore != null) return FishUtils.translateHexColorCodes(overridenLore);
        else {
            if (this.getDisplayName() != null) {
                return this.getDisplayName();
            } else {
                return this.getColour() + "&l" + this.getValue().toUpperCase();
            }

        }
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean hasCompExemptFish() {
        return hasCompExemptFish;
    }

    public void setHasCompExemptFish(boolean hasCompExemptFish) {
        this.hasCompExemptFish = hasCompExemptFish;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }
}
