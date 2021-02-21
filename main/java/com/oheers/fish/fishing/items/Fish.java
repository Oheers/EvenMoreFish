package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import dev.dbassett.skullcreator.SkullCreator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Fish {

    String name;
    Rarity rarity;
    ItemStack type;
    Player fisherman;
    Float length;

    // @TODO add biome-rarity support

    public Fish(Rarity rarity, Player fisher) {
        Names names = new Names();

        this.rarity = rarity;
        this.name = names.get(rarity);
        this.type = getType();
        this.fisherman = fisher;

        setSize();
    }

    // Using translate method over the enum values for the sake of a future config file.

    public ItemStack getItem() {
        ItemStack fish = this.type;
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

        // are min & max size changed? If not, there's no fish-specific value. Check the rarity's value
        if (minSize == 0.0 && maxSize == 0.0) {
            minSize = EvenMoreFish.raritiesFile.getConfig().getDouble("rarities." + this.rarity.getValue() + ".size.minSize");
            maxSize = EvenMoreFish.raritiesFile.getConfig().getDouble("rarities." + this.rarity.getValue() + ".size.maxSize");
        }

        // If there's no rarity-specific value (or max is smaller than min), to avoid being in a pickle we just set min default to 0 and max default to 10
        if ((minSize == 0.0 && maxSize == 0.0) || minSize > maxSize) {
            minSize = 0.0;
            maxSize = 10.0;
        }

        // Random logic that returns a float to 1dp
        int len = (int) (Math.random() * (maxSize*10 - minSize*10 + 1) + minSize*10);
        this.length = (float) len/10;
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public Player getFisherman() {
        return fisherman;
    }

    public Float getLength() {
        return length;
    }

    private ItemStack getType() {

        String uValue = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".item.head-uuid");
        // The fish has item: uuid selected
        // note - only works for players who have joined the server previously, not sure if this'll make it to release.
        if (uValue != null) {
            System.out.println(uValue);
            return SkullCreator.itemWithUuid(new ItemStack(Material.valueOf("PLAYER_HEAD")), UUID.fromString(uValue));
        }

        // The fish has item: 64 selected
        String bValue = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".item.head-64");
        if (bValue != null) {
            return SkullCreator.itemFromBase64(bValue);
        }

        // The fish has item: material selected
        String mValue = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".item.material");
        if (mValue != null) {
            return new ItemStack(Objects.requireNonNull(Material.getMaterial(mValue)));
        }

        // The fish has no item type specified
        else {
            return new ItemStack(Material.COD);
        }


    }
}
