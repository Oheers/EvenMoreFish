package com.oheers.fish.fishing.items;

import com.oheers.fish.FishUtils;

public class Rarity {

    String value, colour;
    double weight;
    boolean announce;
    boolean fishWeighted;
    public String overridenLore;

    boolean c2021;

    String displayName;

    public String permission;

    public Rarity(String value, String colour, double weight, boolean announce, String overridenLore) {
        this.value = value;
        this.colour = colour;
        this.weight = weight;
        this.announce = announce;
        this.overridenLore = overridenLore;
        this.c2021 = false;
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

    public boolean isC2021() {
        return c2021;
    }

    public void setC2021(boolean c2021) {
        this.c2021 = c2021;
    }

    public String getLorePrep() {
        if (overridenLore != null) return FishUtils.translateHexColorCodes(overridenLore);
        else {
            if (this.getDisplayName() != null) {
                return FishUtils.translateHexColorCodes(this.getDisplayName());
            } else {
                return FishUtils.translateHexColorCodes(this.getColour() + "&l" + this.getValue().toUpperCase());
            }

        }
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
