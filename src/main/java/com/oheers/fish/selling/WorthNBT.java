package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
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

        NBTItem nbtItem = new NBTItem(fish);
        nbtItem.setFloat(nbtlength.toString(),length);
        nbtItem.setString(nbtplayer.toString(),player.toString());
        nbtItem.setString(nbtrarity.toString(),rarity);
        nbtItem.setString(nbtname.toString(),name);

        return nbtItem.getItem();
    }

    public static void setNBT(Skull fish, Float length, UUID player, String rarity, String name) {
        // creates key and plops in the value of "value"
        NamespacedKey nbtlength = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-length");
        NamespacedKey nbtplayer = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-player");
        NamespacedKey nbtrarity = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-name");

        NBTTileEntity nbtItem = new NBTTileEntity(fish);
        nbtItem.setFloat(nbtlength.toString(),length);
        if (player != null) nbtItem.setString(nbtplayer.toString(),player.toString());
        nbtItem.setString(nbtrarity.toString(),rarity);
        nbtItem.setString(nbtname.toString(),name);
    }


    public static double getValue(ItemStack item) {
        // creating the key to check for
        NamespacedKey nbtlength = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-length");
        NamespacedKey nbtrarity = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "emf-fish-name");

        if (item != null) {
            if (item.hasItemMeta()) {
                if (FishUtils.isFish(item)) {
                    NBTItem nbtItem = new NBTItem(item);
                    // it's a fish so it'll definitely have these NBT values
                    Float length = nbtItem.getFloat(nbtlength.toString());
                    String rarity = nbtItem.getString(nbtrarity.toString());
                    String name = nbtItem.getString(nbtname.toString());
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
        NBTItem nbtItem = new NBTItem(defaultGUIItem);
        nbtItem.setByte(key.toString(),Byte.MAX_VALUE);
        return nbtItem.getItem();
    }

    public static boolean isDefault(ItemStack is) {
        NamespacedKey key = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), "default-gui-item");
        return new NBTItem(is).hasKey(key.toString());
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