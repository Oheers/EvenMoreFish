package com.oheers.fish;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public static boolean hasKey(final @NotNull NBTCompound nbtCompound, final String key) {
        NamespacedKey namespacedKey = getNamespacedKey(key);

        //Pre NBT-API PR
        if(Boolean.TRUE.equals(nbtCompound.hasKey("PublicBukkitValues"))) {
            NBTCompound publicBukkitValues = nbtCompound.getCompound("PublicBukkitValues");
            if(Boolean.TRUE.equals(publicBukkitValues.hasKey(namespacedKey.toString())))
                return true;
        }

        //NBT API PR
        if(Boolean.TRUE.equals(nbtCompound.hasKey(namespacedKey.toString())))
            return true;

        //NBT COMPAT
        if(Boolean.TRUE.equals(nbtCompound.hasKey(namespacedKey.getNamespace()))) {
            NBTCompound emfCompound = nbtCompound.getCompound(namespacedKey.getNamespace());
            return Boolean.TRUE.equals(emfCompound.hasKey(namespacedKey.getKey()));
        }

        return false;
    }


    public static @Nullable String getString(final @NotNull NBTCompound nbtCompound, final String key) {
        NamespacedKey namespacedKey = getNamespacedKey(key);
        if(Boolean.TRUE.equals(nbtCompound.hasKey("PublicBukkitValues"))) {
            NBTCompound publicBukkitValues = nbtCompound.getCompound("PublicBukkitValues");
            if(Boolean.TRUE.equals(publicBukkitValues.hasKey(namespacedKey.toString())))
                return publicBukkitValues.getString(namespacedKey.toString());
        }

        //NBT API PR
        if(Boolean.TRUE.equals(nbtCompound.hasKey(namespacedKey.toString())))
            return nbtCompound.getString(namespacedKey.toString());

        //NBT COMPAT
        if(Boolean.TRUE.equals(nbtCompound.hasKey(namespacedKey.getNamespace()))) {
            NBTCompound emfCompound = nbtCompound.getCompound(namespacedKey.getNamespace());
            if(Boolean.TRUE.equals(emfCompound.hasKey(namespacedKey.getKey())))
                return nbtCompound.getString(namespacedKey.getKey());
        }

        return null;
    }

    public static @Nullable Float getFloat(final @NotNull NBTCompound nbtCompound, final String key) {
        NamespacedKey namespacedKey = getNamespacedKey(key);
        if(Boolean.TRUE.equals(nbtCompound.hasKey("PublicBukkitValues"))) {
            NBTCompound publicBukkitValues = nbtCompound.getCompound("PublicBukkitValues");
            if(Boolean.TRUE.equals(publicBukkitValues.hasKey(namespacedKey.toString())))
                return publicBukkitValues.getFloat(namespacedKey.toString());
        }

        //NBT API PR
        if(Boolean.TRUE.equals(nbtCompound.hasKey(namespacedKey.toString())))
            return nbtCompound.getFloat(namespacedKey.toString());

        //NBT COMPAT
        if(Boolean.TRUE.equals(nbtCompound.hasKey(namespacedKey.getNamespace()))) {
            NBTCompound emfCompound = nbtCompound.getCompound(namespacedKey.getNamespace());
            if(Boolean.TRUE.equals(emfCompound.hasKey(namespacedKey.getKey())))
                return nbtCompound.getFloat(namespacedKey.getKey());
        }

        return null;
    }


    @Contract("_ -> new")
    private static @NotNull NamespacedKey getNamespacedKey(final String key) {
        return new NamespacedKey(JavaPlugin.getProvidingPlugin(NbtUtils.class), key);
    }
}
