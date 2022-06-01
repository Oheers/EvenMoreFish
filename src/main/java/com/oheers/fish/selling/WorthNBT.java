package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class WorthNBT {

    public static ItemStack setNBT(ItemStack fish, Float length, UUID player, String rarity, String name) {
        // creates key and plops in the value of "value"
        NamespacedKey nbtlength = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-length");
        NamespacedKey nbtplayer = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-player");
        NamespacedKey nbtrarity = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-name");

        ItemMeta itemMeta = fish.getItemMeta();

        itemMeta.getPersistentDataContainer().set(nbtlength, PersistentDataType.FLOAT, length);
        itemMeta.getPersistentDataContainer().set(nbtplayer, PersistentDataType.STRING, player.toString());
        itemMeta.getPersistentDataContainer().set(nbtrarity, PersistentDataType.STRING, rarity);
        itemMeta.getPersistentDataContainer().set(nbtname, PersistentDataType.STRING, name);

        // sets the nbt and returns it
        fish.setItemMeta(itemMeta);
        return fish;
    }

    public static void setNBT(Skull fish, Float length, UUID player, String rarity, String name) {
        // creates key and plops in the value of "value"
        NamespacedKey nbtlength = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-length");
        NamespacedKey nbtplayer = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-player");
        NamespacedKey nbtrarity = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-name");

        PersistentDataContainer itemMeta = fish.getPersistentDataContainer();

        itemMeta.set(nbtlength, PersistentDataType.FLOAT, length);
        if (player != null) itemMeta.set(nbtplayer, PersistentDataType.STRING, player.toString());
        itemMeta.set(nbtrarity, PersistentDataType.STRING, rarity);
        itemMeta.set(nbtname, PersistentDataType.STRING, name);
    }


    public static double getValue(ItemStack item) {
        // creating the key to check for
        NamespacedKey nbtlength = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-length");
        NamespacedKey nbtrarity = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-name");

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
                    int setVal;

                    try {
                        setVal = EvenMoreFish.fishFile.getConfig().getInt("fish." + rarity + "." + name + ".set-worth");
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
        NamespacedKey key = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "default-gui-item");
        ItemMeta itemMeta = defaultGUIItem.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, Byte.MAX_VALUE);
        // sets the nbt and returns it
        defaultGUIItem.setItemMeta(itemMeta);
        return defaultGUIItem;
    }

    public static boolean isDefault(ItemStack is) {
        NamespacedKey key = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "default-gui-item");
        if (is.hasItemMeta()) {
            PersistentDataContainer container = is.getItemMeta().getPersistentDataContainer();
            return container.has(key, PersistentDataType.BYTE);
        }

        return false;
    }

    private static double getMultipliedValue(Float length, String rarity, String name) {
        double value = 0.0;

        value = EvenMoreFish.fishFile.getConfig().getDouble("fish." + rarity + "." + name + ".worth-multiplier");
        // Is there a value set for the specific fish?
        if (value == 0.0) {
            value = EvenMoreFish.raritiesFile.getConfig().getDouble("rarities." + rarity + ".worth-multiplier");
        }

        // Whatever it finds the value to be, gets multiplied by the fish length and set
        value *= length;
        // Sorts out funky decimals during the above multiplication.
        value = Math.round(value*10.0)/10.0;

        return value;
    }
}