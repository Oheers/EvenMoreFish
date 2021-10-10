package com.oheers.fish.fishing.items;

import org.bukkit.ChatColor;

public class Rarity {

    String value, colour;
    double weight;
    boolean announce;
    boolean fishWeighted;
    public String overridenLore;

    public String permission;

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

    public String getLorePrep() {
        if (overridenLore != null) return ChatColor.translateAlternateColorCodes('&', overridenLore);
        else return ChatColor.translateAlternateColorCodes('&', this.getColour() + "&l" + this.getValue().toUpperCase());
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
