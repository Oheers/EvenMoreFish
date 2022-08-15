package com.oheers.fish.fishing.items;

import com.oheers.fish.FishUtils;

public class Rarity {

    public String overridenLore;
    public String permission;
    String value, colour;
    double weight;
    boolean announce;
    boolean fishWeighted;
    boolean hasCompExemptFish;
    String displayName;

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
}
