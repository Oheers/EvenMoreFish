package com.eejayy.fish.fishing.items;

public class Rarity {

    String value, colour;
    double weight;

    public Rarity(String value, String colour, Double weight) {
        this.value = value;
        this.colour = colour;
        this.weight = weight;
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
}
