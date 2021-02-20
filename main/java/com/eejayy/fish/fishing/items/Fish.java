package com.eejayy.fish.fishing.items;

import com.eejayy.fish.EvenMoreFish;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class Fish {

    String name;
    Rarity rarity;
    Material type;
    Player fisherman;
    Float length;

    // @TODO add biome-rarity support

    public Fish(Rarity rarity, Player fisher) {
        Names names = new Names();

        this.rarity = rarity;
        this.name = names.get(rarity);
        this.type = Material.COD;
        this.fisherman = fisher;

        setSize();
    }

    // Using translate method over the enum values for the sake of a future config file.

    public ItemStack getItem() {
        ItemStack fish = new ItemStack(type);
        ItemMeta fishMeta = fish.getItemMeta();
        List<String> lore = Arrays.asList(
                ChatColor.WHITE + "Caught by " + fisherman.getName(),
                ChatColor.WHITE + "Measures " + Float.toString(length) + "cm",
                " ",
                ChatColor.translateAlternateColorCodes('&', rarity.getColour() + "&l") + rarity.getValue().toUpperCase()

        );

        fishMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rarity.getColour() + name));
        fishMeta.setLore(lore);
        fish.setItemMeta(fishMeta);

        return fish;
    }

    private void setSize() {
        double minSize = EvenMoreFish.fishFile.getConfig().getDouble("fish." + this.rarity.getValue() + "." + this.name + ".size.minSize");
        double maxSize = EvenMoreFish.fishFile.getConfig().getDouble("fish." + this.rarity.getValue() + "." + this.name + ".size.maxSize");

        if (minSize == 0.0 && maxSize == 0.0) {
            minSize = EvenMoreFish.raritiesFile.getConfig().getDouble("rarities." + this.rarity.getValue() + ".size.minSize");
            maxSize = EvenMoreFish.raritiesFile.getConfig().getDouble("rarities." + this.rarity.getValue() + ".size.maxSize");
        }

        if ((minSize == 0.0 && maxSize == 0.0) || minSize > maxSize) {
            minSize = 0.0;
            maxSize = 10.0;
        }

        int len = (int) (Math.random() * (maxSize*10 - minSize*10 + 1) + minSize*10);
        this.length = (float) len/10;
    }
}
