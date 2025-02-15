package com.oheers.fish.utils;

import com.oheers.fish.FishUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemUtils {

    public static @NotNull Material getMaterial(@Nullable String materialName, @NotNull Material defaultMaterial) {
        Material material = getMaterial(materialName);
        if (material == null) {
            return defaultMaterial;
        }
        return material;
    }

    public static @Nullable Material getMaterial(@Nullable String materialName) {
        if (materialName == null) {
            return null;
        }
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static boolean isValidMaterial(@Nullable String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return false;
        }
        try {
            Material.valueOf(materialName.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static void glowify(@NotNull ItemStack item) {
        // plops on the unbreaking 1 enchantment to make it glow
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        FishUtils.editMeta(item, meta -> meta.addItemFlags(ItemFlag.HIDE_ENCHANTS));
    }

    /**
     * Creates a new ItemStack with the provided material and applies the old item's meta to it
     * @param item The old item
     * @param material The material to change to
     * @return The provided ItemStack with a different material
     */
    public static ItemStack changeMaterial(@NotNull ItemStack item, @NotNull Material material) {
        ItemStack newItem = new ItemStack(material);
        newItem.setItemMeta(item.getItemMeta());
        return newItem;
    }

}
