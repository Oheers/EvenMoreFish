package com.oheers.fish.fishing.items;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.requirement.Requirement;

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
    Requirement requirement = new Requirement();

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

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
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

    public Requirement getRequirement() {
        return requirement;
    }
}
