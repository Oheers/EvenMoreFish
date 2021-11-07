package com.oheers.fish.selling;

import com.oheers.fish.FishUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WorthNBT {

    public static ItemStack setNBT(ItemStack fish, Float length, String rarity, String name) {
        // creates key and plops in the value of "value"
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");
        NamespacedKey nbtrarity = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-name");

        ItemMeta itemMeta = fish.getItemMeta();

        itemMeta.getPersistentDataContainer().set(nbtlength, PersistentDataType.FLOAT, length);
        itemMeta.getPersistentDataContainer().set(nbtrarity, PersistentDataType.STRING, rarity);
        itemMeta.getPersistentDataContainer().set(nbtname, PersistentDataType.STRING, name);

        // sets the nbt and returns it
        fish.setItemMeta(itemMeta);
        return fish;
    }

    public static Skull setNBT(Skull fish, Float length, String rarity, String name) {
        // creates key and plops in the value of "value"
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");
        NamespacedKey nbtrarity = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-name");

        PersistentDataContainer itemMeta = fish.getPersistentDataContainer();

        itemMeta.set(nbtlength, PersistentDataType.FLOAT, length);
        itemMeta.set(nbtrarity, PersistentDataType.STRING, rarity);
        itemMeta.set(nbtname, PersistentDataType.STRING, name);

        return fish;
    }


    public static double getValue(ItemStack item) {
        // creating the key to check for
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");
        NamespacedKey nbtrarity = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-name");

        if (item != null) {
            if (item.hasItemMeta()) {
                ItemMeta itemMeta = item.getItemMeta();
                PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                if (FishUtils.isFish(item)) {
                    // it's a fish so it'll definitely have these NBT values
                    Float length = container.get(nbtlength, PersistentDataType.FLOAT);
                    String rarity = container.get(nbtrarity, PersistentDataType.STRING);
                    String name = container.get(nbtname, PersistentDataType.STRING);
                    // gets a possible set-worth in the fish.yml
                    FileConfiguration config = FishUtils.findConfigFile(rarity);
                    int setVal;

                    try {
                        setVal = FishUtils.findConfigFile(rarity).getInt("fish." + rarity + "." + name + ".set-worth");
                    } catch (NullPointerException npe) {
                        setVal = 0;
                    }

                    if (setVal != 0) return setVal;
                    // there's no set-worth so we're calculating the worth ourselves
                    return getMultipliedValue(
                            length,
                            rarity,
                            name);
                } else return -1.0;
            } else {
                return -1.0;
            }
        } else return -1.0;
    }

    public static ItemStack attributeDefault(ItemStack defaultGUIItem) {
        NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "default-gui-item");
        ItemMeta itemMeta = defaultGUIItem.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, Byte.MAX_VALUE);
        // sets the nbt and returns it
        defaultGUIItem.setItemMeta(itemMeta);
        return defaultGUIItem;
    }

    public static boolean isDefault(ItemStack is) {
        NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "default-gui-item");
        if (is.hasItemMeta()) {
            PersistentDataContainer container = is.getItemMeta().getPersistentDataContainer();
            return container.has(key, PersistentDataType.BYTE);
        }

        return false;
    }

    private static double getMultipliedValue(Float length, String rarity, String name) {
        double value = 0.0;

        FileConfiguration config = FishUtils.findConfigFile(rarity);
        if (config != null) value = config.getDouble("fish." + rarity + "." + name + ".worth-multiplier");

        // Is there a value set for the specific fish?
        if (value == 0.0) {
            config = FishUtils.findRarityFile(rarity);
            if (config != null) value = config.getDouble("rarities." + rarity + ".worth-multiplier");
        }

        // Whatever it finds the value to be, gets multiplied by the fish length and set
        value *= length;
        // Sorts out funky decimals during the above multiplication.
        value = Math.round(value*10.0)/10.0;

        return value;
    }
}