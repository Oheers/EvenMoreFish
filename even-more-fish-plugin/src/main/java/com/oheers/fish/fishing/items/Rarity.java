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
    boolean useConfigCasing;
    String displayName;
    List<Requirement> requirements = new ArrayList<>();

    public Rarity(String value, String colour, double weight, boolean announce, boolean useConfigCasing, String overridenLore) {
        this.value = value;
        this.colour = colour;
        this.weight = weight;
        this.announce = announce;
        this.overridenLore = overridenLore;
        this.useConfigCasing = useConfigCasing;
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

    public boolean getUseConfigCasing() { return this.useConfigCasing; }

    public boolean isFishWeighted() {
        return fishWeighted;
    }

    public void setFishWeighted(boolean fishWeighted) {
        this.fishWeighted = fishWeighted;
    }

    public String getDisplayName() {
        if (displayName == null) {
            return value;
        }
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
        if (overridenLore != null) return FishUtils.translateColorCodes(overridenLore);
        else {
            if (this.displayName != null) {
                return this.displayName;
            } else {
                String finalName = this.getValue();
                if (!useConfigCasing) {
                    finalName = finalName.toUpperCase();
                }
                return this.getColour() + "&l" + finalName;
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
