package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class WorthNBT {

    public static ItemStack setNBT(ItemStack fish, Double value) {
        // creates key and plops in the value of "value"
        NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-value");
        ItemMeta itemMeta = fish.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, value);
        // sets the nbt and returns it
        fish.setItemMeta(itemMeta);
        return fish;
    }

    public static double getValue(ItemStack item) {
        // creating the key to check for
        NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-value");
        if (item != null) {
            if (item.hasItemMeta()) {
                ItemMeta itemMeta = item.getItemMeta();
                PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                double foundValue = -1.0;

                // setting foundValue if the key exists, if not it gets left at -1, in which case it
                // isn't an EMF fish.
                if (container.has(key , PersistentDataType.DOUBLE)) {
                    foundValue = container.get(key, PersistentDataType.DOUBLE);
                }
                return foundValue;
            } else {
                return -1.0;
            }
        } else {
            return -1.0;
        }
    }

    // checks for the "emf-fish-value" nbt tag, to determine if this itemstack is a fish or not.
    public static boolean isFish(ItemStack i) {
        NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-value");

        if (i != null) {
            if (i.hasItemMeta()) {
                if (i.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
                    return true;
                }
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
}
