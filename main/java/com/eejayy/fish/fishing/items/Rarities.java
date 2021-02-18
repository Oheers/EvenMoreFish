package com.eejayy.fish.fishing.items;

import java.util.Arrays;
import java.util.GregorianCalendar;

public enum Rarities {

    COMMON("&7"),
    UNCOMMON("&b"),
    EPIC("&d"),
    LEGENDARY("&6");

    public final String code;

    private Rarities(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    // A temporary way of choosing a random fish rarity.

    public static Rarities random() {

        switch ((int) (Math.random()*4)) {
            case 1: return UNCOMMON;
            case 2: return EPIC;
            case 3: return LEGENDARY;
            default: return COMMON;
        }
    }

}
