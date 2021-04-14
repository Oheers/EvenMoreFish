package com.oheers.fish.fishing.items;

public class Rarity {

    String value, colour;
    double weight;
    boolean announce;

    public Rarity(String value, String colour, double weight, boolean announce) {
        this.value = value;
        this.colour = colour;
        this.weight = weight;
        this.announce = announce;
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
}
