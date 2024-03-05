package com.oheers.fish.config;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;

public class Xmas2022Config extends ConfigBase {

    private boolean isAvailable = true;
    public HashMap<Integer, Material> fillerDefault = null;
    private static Xmas2022Config instance = null;

    public Xmas2022Config() {
        super("xmas2022.yml");
        instance = this;
    }

    public static Xmas2022Config getInstance() {
        return instance;
    }

    @Override
    public void reload() {
        super.reload();
        if (fillerDefault == null) {
            fillerDefault = new HashMap<>();
        }
        generateDefaultFiller();
    }

    public void generateDefaultFiller() {
        List<String> layoutArray = getConfig().getStringList("gui.advent-calendar.filler-settings.layout");
        List<String> planArray = getConfig().getStringList("gui.advent-calendar.filler-settings.plan");

        for (int i=0; i < layoutArray.size(); i++) {
            String line = layoutArray.get(i);
            for (int j = 0; j < layoutArray.get(i).length(); j++) {
                char materialID = line.charAt(j);
                if (materialID != 'X') {
                    fillerDefault.put(((i * 9) + j), Material.valueOf(planArray.get(Integer.parseInt(String.valueOf(materialID)))));
                }
            }
        }
    }

    public String getGUIName() {
        return getConfig().getString("gui.advent-calendar.title");
    }

    public String getFoundFishName() {
        return getConfig().getString("gui.advent-calendar.filler-settings.fish-name", "&#74d680Day {day} - {name}");
    }

    public String getLockedFishName() {
        return getConfig().getString("gui.advent-calendar.filler-settings.locked-fish-name", "&cDay {day} - ???");
    }

    public List<String> getFoundFishLore() {
        return getConfig().getStringList("gui.advent-calendar.fish-lore");
    }

    public Material getLockedFishMaterial() {
        return Material.valueOf(getConfig().getString("gui.advent-calendar.fish-material", "BARRIER"));
    }

    public List<String> getLockedFishLore() {
        return getConfig().getStringList("gui.advent-calendar.locked-fish-lore");
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
