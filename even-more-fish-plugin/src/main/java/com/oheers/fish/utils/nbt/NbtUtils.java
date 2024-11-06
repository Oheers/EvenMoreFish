package com.oheers.fish.utils.nbt;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NbtUtils {
    private NbtUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean hasKey(final ItemStack item, final String key) {
        final NbtVersion nbtVersion = NbtVersion.getVersion(item);
        final NamespacedKey namespacedKey = getNamespacedKey(key);
        return NBT.get(item, nbt -> {
            return hasKey(nbtVersion,namespacedKey, nbt);
        });
    }

    public static boolean hasKey(final BlockState skull, final String key) {
        final NbtVersion nbtVersion = NbtVersion.getVersion(skull);
        final NamespacedKey namespacedKey = getNamespacedKey(key);

        return NBT.get(skull, nbt -> {
            return hasKey(nbtVersion,namespacedKey, nbt);
        });
    }

    private static boolean hasKey(final @NotNull NbtVersion nbtVersion, final NamespacedKey namespacedKey, final ReadableNBT nbt) {
        return switch (nbtVersion) {
            case NBTAPI -> nbt.hasTag(namespacedKey.toString());
            case LEGACY -> {
                if (nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES)) {
                    yield nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES)
                            .hasTag(namespacedKey.toString());
                }
                yield false;
            }
            case COMPAT -> nbt.getCompound(namespacedKey.getNamespace()).hasTag(namespacedKey.getKey());
        };
    }


    private static @Nullable String getNbtString(NamespacedKey namespacedKey, @NotNull NbtVersion nbtVersion, ReadableItemNBT nbt) {
        return switch (nbtVersion) {
            case NBTAPI -> {
                if (nbt.hasTag(namespacedKey.toString())) {
                    yield nbt.getString(namespacedKey.toString());
                }
                yield null;
            }
            case COMPAT -> nbt.getCompound(namespacedKey.getNamespace()).getString(namespacedKey.getKey());
            case LEGACY -> {
                if (nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES)) {
                    ReadableNBT publicBukkitValues = nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES);
                    if (publicBukkitValues == null) yield null;
                    String baitString = publicBukkitValues.getString(namespacedKey.toString());
                    if (baitString == null) yield null;
                    yield baitString.isEmpty() ? null : baitString;
                }
                yield null;
            }
        };

    }

    @Nullable
    public static String getString(final ItemStack item, final String key) {
        final NbtVersion nbtVersion = NbtVersion.getVersion(item);
        final NamespacedKey namespacedKey = NbtUtils.getNamespacedKey(key);
        return NBT.get(item, nbt -> {
            return getNbtString(namespacedKey, nbtVersion, nbt);
        });
    }

    public static String[] getBaitArray(final ItemStack item) {
        final String appliedBait = NbtUtils.getString(item, NbtKeys.EMF_APPLIED_BAIT);
        if (appliedBait == null) return new String[0];
        return appliedBait.split(",");
    }

    public static @Nullable Float getFloat(final ItemStack item, final String key) {
        final NbtVersion nbtVersion = NbtVersion.getVersion(item);
        final NamespacedKey namespacedKey = NbtUtils.getNamespacedKey(key);
        return NBT.get(item, nbt -> switch (nbtVersion) {
            case NBTAPI -> {
                if (nbt.hasTag(namespacedKey.toString())) {
                    yield nbt.getFloat(namespacedKey.toString());
                }
                yield null;
            }
            case COMPAT -> nbt.getCompound(namespacedKey.getNamespace()).getFloat(namespacedKey.getKey());
            case LEGACY -> {
                if (nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES)) {
                    yield nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES).getFloat(namespacedKey.toString());
                }
                yield null;
            }
        });
    }

    public static @Nullable Integer getInteger(final ItemStack item, final String key) {
        final NbtVersion nbtVersion = NbtVersion.getVersion(item);
        final NamespacedKey namespacedKey = NbtUtils.getNamespacedKey(key);
        return NBT.get(item, nbt -> switch (nbtVersion) {
            case NBTAPI -> {
                if (nbt.hasTag(namespacedKey.toString())) {
                    yield nbt.getInteger(namespacedKey.toString());
                }
                yield null;
            }
            case COMPAT -> nbt.getCompound(namespacedKey.getNamespace()).getInteger(namespacedKey.getKey());
            case LEGACY -> {
                if (nbt.hasTag(NbtKeys.PUBLIC_BUKKIT_VALUES)) {
                    yield nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES).getInteger(namespacedKey.toString());
                }
                yield null;
            }
        });
    }


    @Contract("_ -> new")
    public static @NotNull NamespacedKey getNamespacedKey(final String key) {
        return new NamespacedKey(JavaPlugin.getProvidingPlugin(NbtUtils.class), key);
    }

}