package com.oheers.fish.selling;

import com.oheers.fish.FishUtils;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.config.Xmas2022Config;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WorthNBT {
    private WorthNBT() {
        throw new UnsupportedOperationException();
    }

    public static ItemStack setNBT(ItemStack fishItem, Fish fish) {
        // creates key and plops in the value of "value"
        NBT.modify(fishItem, nbt -> {
            ReadWriteNBT emfCompound = nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND);
            if (fish.getLength() > 0) {
                emfCompound.setFloat(NbtKeys.EMF_FISH_LENGTH, fish.getLength());
            }
            if (!fish.hasFishermanDisabled() && fish.getFisherman() != null) {
                emfCompound.setString(NbtKeys.EMF_FISH_PLAYER, fish.getFisherman().toString());
            }
            emfCompound.setString(NbtKeys.EMF_FISH_NAME, fish.getName());
            emfCompound.setString(NbtKeys.EMF_FISH_RARITY, fish.getRarity().getValue());
            emfCompound.setInteger(NbtKeys.EMF_FISH_RANDOM_INDEX, fish.getFactory().getChosenRandomIndex());
            emfCompound.setBoolean(NbtKeys.EMF_XMAS_FISH, fish.isXmasFish());
        });

        return fishItem;
    }

    public static void setNBT(Skull fishSkull, Fish fish) {
        NamespacedKey nbtlength = NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_LENGTH);
        NamespacedKey nbtplayer = NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_PLAYER);
        NamespacedKey nbtrarity = NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_RARITY);
        NamespacedKey nbtname = NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_NAME);
        NamespacedKey nbtrandomIndex = NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_RANDOM_INDEX);
        NamespacedKey nbtxmasfish = NbtUtils.getNamespacedKey(NbtKeys.EMF_XMAS_FISH);

        //TODO try with NBT-API
        PersistentDataContainer itemMeta = fishSkull.getPersistentDataContainer();

        if (fish.getLength() > 0) {
            itemMeta.set(nbtlength, PersistentDataType.FLOAT, fish.getLength());
        }
        if (fish.getFisherman() != null && !fish.hasFishermanDisabled()) {
            itemMeta.set(nbtplayer, PersistentDataType.STRING, fish.getFisherman().toString());
        }
        itemMeta.set(nbtrandomIndex, PersistentDataType.INTEGER, fish.getFactory().getChosenRandomIndex());
        itemMeta.set(nbtrarity, PersistentDataType.STRING, fish.getRarity().getValue());
        itemMeta.set(nbtname, PersistentDataType.STRING, fish.getName());
        itemMeta.set(nbtxmasfish, PersistentDataType.INTEGER, (fish.isXmasFish()) ? 1 : 0);
    }

    public static double getValue(ItemStack item) {
        // creating the key to check for
        if (!FishUtils.isFish(item)) {
            return -1.0;
        }

        // it's a fish so it'll definitely have these NBT values
        Float length = NbtUtils.getFloat(item, NbtKeys.EMF_FISH_LENGTH);
        String rarity = NbtUtils.getString(item, NbtKeys.EMF_FISH_RARITY);
        String name = NbtUtils.getString(item, NbtKeys.EMF_FISH_NAME);
        Integer xmasINT = NbtUtils.getInteger(item, NbtKeys.EMF_XMAS_FISH);
        boolean isXmasFish = false;

        if (xmasINT != null) {
            isXmasFish = xmasINT == 1;
        }

        // gets a possible set-worth in the fish.yml
        try {
            int configValue;
            if (!isXmasFish) {
                configValue = FishFile.getInstance().getConfig().getInt("fish." + rarity + "." + name + ".set-worth");
            } else {
                configValue = Xmas2022Config.getInstance().getConfig().getInt("fish.Christmas 2022." + name + ".set-worth");
            }

            if (configValue == 0) {
                throw new NullPointerException();
            }
            return configValue;
        } catch (NullPointerException npe) {
            // there's no set-worth so we're calculating the worth ourselves
            return length != null && length > 0 ? getMultipliedValue(length, rarity, name, isXmasFish) : 0;
        }
    }

    public static ItemStack attributeDefault(ItemStack defaultGUIItem) {
        NBT.modify(defaultGUIItem, nbt -> {
            nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND).setByte(NbtKeys.DEFAULT_GUI_ITEM, Byte.MAX_VALUE);
        });
        return defaultGUIItem;
    }

    public static boolean isDefault(ItemStack is) {
        return NbtUtils.hasKey(is, NbtKeys.DEFAULT_GUI_ITEM);
    }

    private static double getMultipliedValue(Float length, String rarity, String name, boolean isXmasFish) {
        double worthMultiplier = getWorthMultiplier(rarity, name, isXmasFish);
        double value = multipleWorthByLength(worthMultiplier, length);
        return sortFunkyDecimals(value);
    }

    private static double getWorthMultiplier(final String rarity, final String name, boolean isXmasFish) {
        double value;
        if (isXmasFish) {
            value = Xmas2022Config.getInstance().getConfig().getDouble("fish.Christmas 2022." + name + ".worth-multiplier");
        } else {
            value = FishFile.getInstance().getConfig().getDouble("fish." + rarity + "." + name + ".worth-multiplier");
        }
        // Is there a value set for the specific fish?
        if (value == 0.0) {
            if (isXmasFish) {
                return Xmas2022Config.getInstance().getConfig().getDouble("rarities.Christmas 2022.worth-multiplier");
            } else {
                return RaritiesFile.getInstance().getConfig().getDouble("rarities." + rarity + ".worth-multiplier");
            }
        }

        return value;
    }

    /**
     * Calculates the worth of an item based on a given worth multiplier and length.
     *
     * @param worthMultiplier The multiplier to apply to the length value.
     * @param length The length value to be multiplied.
     * @return The calculated worth of the item.
     */
    private static double multipleWorthByLength(final double worthMultiplier, final Float length) {
        return worthMultiplier * length;
    }


    /**
     * Sorts a double value by rounding it to the nearest whole number with one decimal place.
     *
     * @param value The double value to be sorted.
     * @return The rounded double value with one decimal place.
     */
    // Sorts out funky decimals during the above multiplication.
    private static double sortFunkyDecimals(final double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}