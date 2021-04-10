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
                System.out.println("found: " + foundValue);
                return foundValue;
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }
}
