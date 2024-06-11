package com.oheers.fish.utils.nbt;

import com.oheers.fish.config.MainConfig;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NbtUtils {


    public static boolean hasKey(final ItemStack item, final String key) {
        final NbtVersion nbtVersion = NbtVersion.getVersion(item);
        final NamespacedKey namespacedKey = getNamespacedKey(key);

        switch (nbtVersion) {
            case NBTAPI:
                return NBT.get(item, nbt -> {
                    return nbt.hasTag(namespacedKey.toString());
                });
            case LEGACY:
                return NBT.get(item, nbt -> {
                    if (nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES)) {
                        return nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES)
                                .hasTag(namespacedKey.toString());
                    }
                    return false;
                });
            case COMPAT:
                return NBT.get(item, nbt -> {
                    return nbt.getCompound(namespacedKey.getNamespace()).hasTag(namespacedKey.getKey());
                });
            default:
                return false;
        }
    }

    public static boolean hasKey(final BlockState skull, final String key) {
        final NbtVersion nbtVersion = NbtVersion.getVersion(skull);
        final NamespacedKey namespacedKey = getNamespacedKey(key);

        switch (nbtVersion) {
            case NBTAPI:
                return NBT.get(skull, nbt -> {
                    return nbt.hasTag(namespacedKey.toString());
                });
            case LEGACY:
                return NBT.get(skull, nbt -> {
                    if (nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES)) {
                        return nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES)
                                .hasTag(namespacedKey.toString());
                    }
                    return false;
                });
            case COMPAT:
                return NBT.get(skull, nbt -> {
                    return nbt.getCompound(namespacedKey.getNamespace()).hasTag(namespacedKey.getKey());
                });
            default:
                return false;
        }
    }

    private static @Nullable String getNbtApiString(final ItemStack item, final String key) {
        //todo, might be compat version instead
        final NamespacedKey namespacedKey = NbtUtils.getNamespacedKey(key);
        return NBT.get(item, nbt -> {
            if (nbt.hasTag(namespacedKey.toString())) {
                return nbt.getString(namespacedKey.toString());
            }
            return null;
        });
    }
    @Nullable
    public static String getString(final ItemStack item, final String key) {
        if (MainConfig.getInstance().getNbtMode().equalsIgnoreCase("optimized")) {
            return getNbtApiString(item,key);
        }

        final NbtVersion nbtVersion = NbtVersion.getVersion(item);
        final NamespacedKey namespacedKey = NbtUtils.getNamespacedKey(key);
        switch (nbtVersion) {
            case NBTAPI: {
                return NBT.get(item, nbt -> {
                    if (nbt.hasTag(namespacedKey.toString())) {
                        return nbt.getString(namespacedKey.toString());
                    }
                    return null;
                });
            }
            case COMPAT: {
                return NBT.get(item, nbt -> {
                    return nbt.getCompound(namespacedKey.getNamespace()).getString(namespacedKey.getKey());
                });
            }
            case LEGACY:{
                return NBT.get(item,nbt -> {
                    if (nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES)) {
                        return nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES).getString(namespacedKey.toString());
                    }
                    return null;
                });
            }
            default:
                return null;
        }
    }

    public static @Nullable Float getFloat(final ItemStack item, final String key) {
        final NbtVersion nbtVersion = NbtVersion.getVersion(item);
        final NamespacedKey namespacedKey = NbtUtils.getNamespacedKey(key);
        switch (nbtVersion) {
            case NBTAPI: {
                return NBT.get(item, nbt -> {
                    if (nbt.hasTag(namespacedKey.toString())) {
                        return nbt.getFloat(namespacedKey.toString());
                    }
                    return null;
                });
            }
            case COMPAT: {
                return NBT.get(item, nbt -> {
                    return nbt.getCompound(namespacedKey.getNamespace()).getFloat(namespacedKey.getKey());
                });
            }
            case LEGACY:{
                return NBT.get(item,nbt -> {
                    if (nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES)) {
                        return nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES).getFloat(namespacedKey.toString());
                    }
                    return null;
                });
            }
            default:
                return null;
        }
    }

    public static @Nullable Integer getInteger(final ItemStack item, final String key) {
        final NbtVersion nbtVersion = NbtVersion.getVersion(item);
        final NamespacedKey namespacedKey = NbtUtils.getNamespacedKey(key);
        switch (nbtVersion) {
            case NBTAPI: {
                return NBT.get(item, nbt -> {
                    if (nbt.hasTag(namespacedKey.toString())) {
                        return nbt.getInteger(namespacedKey.toString());
                    }
                    return null;
                });
            }
            case COMPAT: {
                return NBT.get(item, nbt -> {
                    return nbt.getCompound(namespacedKey.getNamespace()).getInteger(namespacedKey.getKey());
                });
            }
            case LEGACY:{
                return NBT.get(item,nbt -> {
                    if (nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES)) {
                        return nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES).getInteger(namespacedKey.toString());
                    }
                    return null;
                });
            }
            default:
                return null;
        }
    }


    @Contract("_ -> new")
    public static @NotNull NamespacedKey getNamespacedKey(final String key) {
        return new NamespacedKey(JavaPlugin.getProvidingPlugin(NbtUtils.class), key);
    }

}
