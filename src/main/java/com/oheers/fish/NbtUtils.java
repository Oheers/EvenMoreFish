package com.oheers.fish;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class NbtUtils {
    /*
        3 possible options:
        legacy: <-- check for this first
            compound for PublicBukkitValues
        nbt-api-pr: <-- then this
            evenmorefish:key: value
        nbt-compat-pr: <-- then this
            compound for evenmorefish:
                            key: value
     */
    public static boolean hasKey(final @NotNull NBTItem nbtItem, final String key) {
        NamespacedKey namespacedKey = getNamespacedKey(key);

        //Pre NBT-API PR
        if(Boolean.TRUE.equals(nbtItem.hasKey("PublicBukkitValues"))) {
            NBTCompound publicBukkitValues = nbtItem.getCompound("PublicBukkitValues");
            if(Boolean.TRUE.equals(publicBukkitValues.hasKey(namespacedKey.toString())))
                return true;
        }

        //NBT API PR
        if(Boolean.TRUE.equals(nbtItem.hasKey(namespacedKey.toString())))
            return true;

        //NBT COMPAT
        if(Boolean.TRUE.equals(nbtItem.hasKey(namespacedKey.getNamespace()))) {
            NBTCompound emfCompound = nbtItem.getCompound(namespacedKey.getNamespace());
            return Boolean.TRUE.equals(emfCompound.hasKey(namespacedKey.getKey()));
        }

        return false;
    }

    @Contract("_ -> new")
    private static @NotNull NamespacedKey getNamespacedKey(final String key) {
        return new NamespacedKey(JavaPlugin.getProvidingPlugin(NbtUtils.class), key);
    }
}
