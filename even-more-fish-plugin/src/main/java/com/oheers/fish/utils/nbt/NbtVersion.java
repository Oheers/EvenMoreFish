package com.oheers.fish.utils.nbt;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

/**
 * Enum representing the different versions of NBT (Named Binary Tag) data.
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
            if (Boolean.TRUE.equals(nbt.hasTag(NbtKeys.EMF_COMPOUND))) {
                return NbtVersion.COMPAT; //def an emf item
            }
            if (Boolean.TRUE.equals(nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES))) {
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
            if (Boolean.TRUE.equals(nbt.hasTag(NbtKeys.EMF_COMPOUND))) {
                return NbtVersion.COMPAT; //def an emf item
            }
            if (Boolean.TRUE.equals(nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES))) {
                return NbtVersion.LEGACY;
            }
            return NbtVersion.NBTAPI;
        });
    }
}
