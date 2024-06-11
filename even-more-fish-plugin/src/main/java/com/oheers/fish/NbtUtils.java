package com.oheers.fish;

import com.oheers.fish.config.MainConfig;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

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
                    if (nbt.hasTag(Keys.PUBLIC_BUKKIT_VALUES)) {
                        return nbt.getCompound(Keys.PUBLIC_BUKKIT_VALUES)
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
                    if (nbt.hasTag(Keys.PUBLIC_BUKKIT_VALUES)) {
                        return nbt.getCompound(Keys.PUBLIC_BUKKIT_VALUES)
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
                    if (nbt.hasTag(Keys.PUBLIC_BUKKIT_VALUES)) {
                        return nbt.getCompound(Keys.PUBLIC_BUKKIT_VALUES).getString(namespacedKey.toString());
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
                    if (nbt.hasTag(Keys.PUBLIC_BUKKIT_VALUES)) {
                        return nbt.getCompound(Keys.PUBLIC_BUKKIT_VALUES).getFloat(namespacedKey.toString());
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
                    if (nbt.hasTag(Keys.PUBLIC_BUKKIT_VALUES)) {
                        return nbt.getCompound(Keys.PUBLIC_BUKKIT_VALUES).getInteger(namespacedKey.toString());
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


    /**
     * Enum representing the different versions of NBT (Named Binary Tag) data.
     *
     * @author Your Name
     */
    public enum NbtVersion {
        /**
         * Represents the legacy version of NBT data.
         */
        LEGACY,

        /**
         * Represents the version of NBT data that uses the nbt-api.
         */
        NBTAPI,

        /**
         * Represents the compatible version of NBT data.
         */
        COMPAT;

        /**
         * Returns the version of NBT data associated with the given ItemStack.
         *
         * @param itemStack The ItemStack to check.
         * @return The NbtVersion representing the version of the NBT data.
         */
        public static NbtVersion getVersion(final ItemStack itemStack) {
            return NBT.get(itemStack, nbt -> {
                if (Boolean.TRUE.equals(nbt.hasTag(Keys.EMF_COMPOUND))) {
                    return NbtVersion.COMPAT; //def an emf item
                }
                if (Boolean.TRUE.equals(nbt.hasTag(Keys.PUBLIC_BUKKIT_VALUES))) {
                    return NbtVersion.LEGACY;
                }
                return NbtVersion.NBTAPI;
            });
        }

        /**
         * Returns the version of NBT data associated with the given BlockState.
         *
         * @param state The BlockState to check.
         * @return The NbtVersion representing the version of the NBT data.
         */
        public static NbtVersion getVersion(final BlockState state) {
            return NBT.get(state, nbt -> {
                if (Boolean.TRUE.equals(nbt.hasTag(Keys.EMF_COMPOUND))) {
                    return NbtVersion.COMPAT; //def an emf item
                }
                if (Boolean.TRUE.equals(nbt.hasTag(Keys.PUBLIC_BUKKIT_VALUES))) {
                    return NbtVersion.LEGACY;
                }
                return NbtVersion.NBTAPI;
            });
        }
    }

    public static class Keys {
        public static final String EMF_COMPOUND = JavaPlugin.getProvidingPlugin(NbtUtils.class).getName().toLowerCase(Locale.ROOT);
        public static final String EMF_FISH_PLAYER = "emf-fish-player";
        public static final String EMF_FISH_RARITY = "emf-fish-rarity";
        public static final String EMF_FISH_LENGTH = "emf-fish-length";
        public static final String EMF_FISH_NAME = "emf-fish-name";
        public static final String EMF_XMAS_FISH = "emf-xmas-fish";
        public static final String EMF_FISH_RANDOM_INDEX = "emf-fish-random-index";
        public static final String EMF_BAIT = "emf-bait";
        public static final String EMF_APPLIED_BAIT = "emf-applied-bait";

        public static final String EMF_ROD_NBT = "emf-rod-nbt";

        public static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
        public static final String DEFAULT_GUI_ITEM = "default-gui-item";
    }
}
