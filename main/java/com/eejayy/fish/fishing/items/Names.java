package com.eejayy.fish.fishing.items;

import java.util.*;

import static com.eejayy.fish.fishing.items.Rarities.*;

public class Names {

    public static List<String> commons = Arrays.asList("Gary", "Tim", "John");
    public static List<String> uncommons = Arrays.asList("Sarah", "Becky", "Anna");
    public static List<String> epics = Arrays.asList("Alan", "Philip", "Ronald");
    public static List<String> legs = Arrays.asList("Demi", "Sabrina", "Julia");

    public static String get(Rarities r) {
        int ran = (int) (Math.random()*3);
        switch (r) {
            case COMMON: return commons.get(ran);
            case UNCOMMON: return uncommons.get(ran);
            case EPIC: return epics.get(ran);
            case LEGENDARY: return legs.get(ran);
            default: return "aaa";
        }
    }

}
