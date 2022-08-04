package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.NbtUtils;
import com.oheers.fish.fishing.items.Fish;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class WorthNBT {

    public static ItemStack setNBT(ItemStack fishItem, Fish fish) {
        // creates key and plops in the value of "value"
        NBTItem nbtItem = new NBTItem(fishItem);

        NBTCompound emfCompound = nbtItem.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
        if (fish.getLength() > 0)
            emfCompound.setFloat(NbtUtils.Keys.EMF_FISH_LENGTH, fish.getLength());
        if (!fish.hasFishermanDisabled())
            emfCompound.setString(NbtUtils.Keys.EMF_FISH_PLAYER, fish.getFisherman().toString());
        emfCompound.setString(NbtUtils.Keys.EMF_FISH_NAME, fish.getName());
        emfCompound.setString(NbtUtils.Keys.EMF_FISH_RARITY, fish.getRarity().getValue());
        emfCompound.setInteger(NbtUtils.Keys.EMF_FISH_RANDOM_INDEX, fish.getFactory().getChosenRandomIndex());

        return nbtItem.getItem();
    }

    public static void setNBT(Skull fishSkull, Fish fish) {
        NamespacedKey nbtlength = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), NbtUtils.Keys.EMF_FISH_LENGTH);
        NamespacedKey nbtplayer = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), NbtUtils.Keys.EMF_FISH_PLAYER);
        NamespacedKey nbtrarity = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), NbtUtils.Keys.EMF_FISH_RARITY);
        NamespacedKey nbtname = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), NbtUtils.Keys.EMF_FISH_NAME);
        NamespacedKey nbtrandomIndex = new NamespacedKey(JavaPlugin.getProvidingPlugin(WorthNBT.class), NbtUtils.Keys.EMF_FISH_RANDOM_INDEX);

        PersistentDataContainer itemMeta = fishSkull.getPersistentDataContainer();

        if (fish.getLength() > 0)
            itemMeta.set(nbtlength, PersistentDataType.FLOAT, fish.getLength());
        if (fish.getFisherman() != null && !fish.hasFishermanDisabled())
            itemMeta.set(nbtplayer, PersistentDataType.STRING, fish.getFisherman().toString());
        itemMeta.set(nbtrandomIndex, PersistentDataType.INTEGER, fish.getFactory().getChosenRandomIndex());
        itemMeta.set(nbtrarity, PersistentDataType.STRING, fish.getRarity().getValue());
        itemMeta.set(nbtname, PersistentDataType.STRING, fish.getName());
    }

    public static double getValue(ItemStack item) {
        // creating the key to check for
        if (!FishUtils.isFish(item)) {
            return -1.0;
        }


        NBTItem nbtItem = new NBTItem(item);
        // it's a fish so it'll definitely have these NBT values
        Float length = NbtUtils.getFloat(nbtItem, NbtUtils.Keys.EMF_FISH_LENGTH);
        String rarity = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_RARITY);
        String name = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_NAME);


        // gets a possible set-worth in the fish.yml
        try {
            int configValue = EvenMoreFish.fishFile.getConfig().getInt("fish." + rarity + "." + name + ".set-worth");
            EvenMoreFish.getInstance().getLogger().info("Config value is " + configValue);
            if (configValue == 0)
                throw new NullPointerException();
            return configValue;
        } catch (NullPointerException npe) {
            // there's no set-worth so we're calculating the worth ourselves
            return length != null && length > 0 ? getMultipliedValue(length, rarity, name) : 0;
        }
    }

    public static ItemStack attributeDefault(ItemStack defaultGUIItem) {
        NBTItem nbtItem = new NBTItem(defaultGUIItem);
        NBTCompound emfCompound = nbtItem.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
        emfCompound.setByte(NbtUtils.Keys.DEFAULT_GUI_ITEM, Byte.MAX_VALUE);
        return nbtItem.getItem();
    }

    public static boolean isDefault(ItemStack is) {
        return NbtUtils.hasKey(new NBTItem(is), NbtUtils.Keys.DEFAULT_GUI_ITEM);
    }

    private static double getMultipliedValue(Float length, String rarity, String name) {
        double worthMultiplier = getWorthMultiplier(rarity, name);
        double value = multipleWorthByLength(worthMultiplier, length);
        return sortFunkyDecimals(value);
    }

    private static double getWorthMultiplier(final String rarity, final String name) {
        double value = EvenMoreFish.fishFile.getConfig().getDouble("fish." + rarity + "." + name + ".worth-multiplier");
        // Is there a value set for the specific fish?
        if (value == 0.0) {
            return EvenMoreFish.raritiesFile.getConfig().getDouble("rarities." + rarity + ".worth-multiplier");
        }

        return value;
    }

    private static double multipleWorthByLength(final double worthMultiplier, final Float length) {
        return worthMultiplier * length;
    }

    // Sorts out funky decimals during the above multiplication.
    private static double sortFunkyDecimals(final double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}