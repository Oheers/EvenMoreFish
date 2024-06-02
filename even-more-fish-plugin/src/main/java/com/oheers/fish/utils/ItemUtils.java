package com.oheers.fish.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemUtils {

    public static @NotNull Material getMaterial(@NotNull String materialName, @NotNull Material defaultMaterial) {
        Material material = getMaterial(materialName);
        if (material == null) {
            return defaultMaterial;
        }
        return material;
    }

    public static @Nullable Material getMaterial(@NotNull String materialName) {
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static boolean isValidMaterial(String materialName) {
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

    public static void glowify(ItemStack i) {
        // plops on the unbreaking 1 enchantment to make it glow
        i.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta meta = i.getItemMeta();
        if (meta != null) {
            // hides the unbreaking 1 enchantment from showing in the lore
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            i.setItemMeta(meta);
        }
    }

}
