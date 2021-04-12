package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.items.Rarity;
import com.sun.tools.javac.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

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

    public static double getValue(ItemStack item) {
        // creating the key to check for
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");
        NamespacedKey nbtrarity = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-name");

        if (item != null) {
            if (item.hasItemMeta()) {
                ItemMeta itemMeta = item.getItemMeta();
                PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                double foundValue = -1.0;

                // setting foundValue if the keys exist, if not it gets left at -1, in which case it
                // isn't an EMF fish.
                if (container.has(nbtlength , PersistentDataType.FLOAT) &&
                    container.has(nbtrarity , PersistentDataType.STRING) &&
                    container.has(nbtname , PersistentDataType.STRING))
                {
                    foundValue = getMultipliedValue(
                            container.get(nbtlength, PersistentDataType.FLOAT),
                            container.get(nbtrarity, PersistentDataType.STRING),
                            container.get(nbtname, PersistentDataType.STRING));
                }

                return foundValue;
            } else {
                return -1.0;
            }
        } else {
            return -1.0;
        }
    }

    // checks for the "emf-fish-length" nbt tag, to determine if this itemstack is a fish or not.
    // we only need to check for the length since they're all added in a batch if it's an EMF fish
    public static boolean isFish(ItemStack i) {
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");

        if (i != null) {
            if (i.hasItemMeta()) {
                return i.getItemMeta().getPersistentDataContainer().has(nbtlength, PersistentDataType.FLOAT);
            }
        }

        return false;
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
        double value;
        value = EvenMoreFish.fishFile.getConfig().getDouble("fish." + rarity + "." + name + ".worth-multiplier");

        // Is there a value set for the specific fish?
        if (value == 0.0) {
            value = EvenMoreFish.raritiesFile.getConfig().getDouble("rarities." + rarity + ".worth-multiplier");
        }

        // Whatever it finds the value to be, gets multiplied by the fish length and set
        value *= length;
        // Sorts out funky decimals during the above multiplication.
        value = Math.round(value*10.0)/10.0;
        System.out.println("length: " + length);
        System.out.println("rarity: " + rarity);
        System.out.println("name: " + name);
        System.out.println("value: " + value);

        return value;
    }
}
