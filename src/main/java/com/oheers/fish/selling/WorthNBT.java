package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.NbtUtils;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WorthNBT {

    public static ItemStack setNBT(ItemStack fish, Float length, @NotNull UUID player, String rarity, String name) {
        // creates key and plops in the value of "value"
        NBTItem nbtItem = new NBTItem(fish);

        NBTCompound emfCompound = nbtItem.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
        emfCompound.setFloat(NbtUtils.Keys.EMF_FISH_LENGTH, length);
        emfCompound.setString(NbtUtils.Keys.EMF_FISH_PLAYER, player.toString());
        emfCompound.setString(NbtUtils.Keys.EMF_FISH_NAME, name);
        emfCompound.setString(NbtUtils.Keys.EMF_FISH_RARITY, rarity);
        nbtItem.setObject(NbtUtils.Keys.EMF_COMPOUND, emfCompound);

        return nbtItem.getItem();
    }

    public static void setNBT(Skull fish, Float length, UUID player, String rarity, String name) {
        // creates key and plops in the value of "value"
        NBTTileEntity nbtItem = new NBTTileEntity(fish);
        NBTCompound emfCompound = nbtItem.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
        emfCompound.setFloat(NbtUtils.Keys.EMF_FISH_LENGTH, length);

        if (player != null) {
            emfCompound.setString(NbtUtils.Keys.EMF_FISH_PLAYER, player.toString());
        }
        emfCompound.setString(NbtUtils.Keys.EMF_FISH_NAME, name);
        emfCompound.setString(NbtUtils.Keys.EMF_FISH_RARITY, rarity);
        nbtItem.setObject(NbtUtils.Keys.EMF_COMPOUND, emfCompound);
    }


    public static double getValue(ItemStack item) {
        // creating the key to check for
        if (item == null || !item.hasItemMeta() || !FishUtils.isFish(item)) {
            return -1.0;
        }


        NBTItem nbtItem = new NBTItem(item);
        // it's a fish so it'll definitely have these NBT values
        Float length = NbtUtils.getFloat(nbtItem, NbtUtils.Keys.EMF_FISH_LENGTH);
        String rarity = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_RARITY);
        String name = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_NAME);


        // gets a possible set-worth in the fish.yml
        try {
            return EvenMoreFish.fishFile.getConfig().getInt("fish." + rarity + "." + name + ".set-worth");
        } catch (NullPointerException npe) {
            // there's no set-worth so we're calculating the worth ourselves
            return getMultipliedValue(length, rarity, name);
        }
    }

    public static ItemStack attributeDefault(ItemStack defaultGUIItem) {
        NBTItem nbtItem = new NBTItem(defaultGUIItem);
        NBTCompound emfCompound = nbtItem.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
        emfCompound.setByte(NbtUtils.Keys.DEFAULT_GUI_ITEM, Byte.MAX_VALUE);
        nbtItem.setObject(NbtUtils.Keys.EMF_COMPOUND, emfCompound);
        return nbtItem.getItem();
    }

    public static boolean isDefault(ItemStack is) {
        return NbtUtils.hasKey(new NBTItem(is), NbtUtils.Keys.DEFAULT_GUI_ITEM);
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
        value = Math.round(value * 10.0) / 10.0;

        return value;
    }
}