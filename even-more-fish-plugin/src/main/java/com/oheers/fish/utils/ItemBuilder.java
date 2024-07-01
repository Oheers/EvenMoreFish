package com.oheers.fish.utils;

import com.oheers.fish.config.messages.Message;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemBuilder {

    private Material material;
    private String display = null;
    private List<String> lore = new ArrayList<>();
    private boolean glowing = false;

    public ItemBuilder(@NotNull Material material) {
        this.material = material;
    }

    public ItemBuilder(@NotNull String materialName, @NotNull Material defaultMaterial) {
        this.material = ItemUtils.getMaterial(materialName, defaultMaterial);
    }

    /**
     * Build an ItemBuilder from a ConfigurationSection object.
     * @param section The ConfigurationSection for the item.
     * @param defaultMaterial The default material to use, if the configured material is invalid.
     */
    public ItemBuilder(@NotNull ConfigurationSection section, @NotNull Material defaultMaterial) {
        // Material
        this.material = ItemUtils.getMaterial(section.getString("material", defaultMaterial.toString()), defaultMaterial);

        // Display
        String display = section.getString("display");
        if (display != null) {
            this.display = display;
        }

        // Lore
        this.lore = section.getStringList("lore");

        // Glowing
        this.glowing = section.getBoolean("glowing");
    }

    public ItemBuilder withMaterial(@NotNull Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder withMaterial(@NotNull String materialName, @NotNull Material defaultMaterial) {
        this.material = ItemUtils.getMaterial(materialName, defaultMaterial);
        return this;
    }

    public ItemBuilder withDisplay(@NotNull String display) {
        if (replacer != null) {
            display = replacer.replace(display);
        }
        this.display = display;
        return this;
    }

    public ItemBuilder withLore(@NotNull List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder addLore(@NotNull String line) {
        this.lore.add(line);
        return this;
    }

    public ItemBuilder addLore(@NotNull List<String> lines) {
        this.lore.addAll(lines);
        return this;
    }

    public ItemBuilder setGlowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }

    public ItemStack build() {
        if (this.material == null) {
            return null;
        }
        ItemStack stack = new ItemStack(this.material);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        if (this.display != null) {
            meta.setDisplayName(new Message(this.display).getRawMessage(false));
        }
        if (!this.lore.isEmpty()) {
            meta.setLore(new Message(this.lore).getRawListMessage(false));
        }
        stack.setItemMeta(meta);
        if (this.glowing) {
            ItemUtils.glowify(stack);
        }
        return stack;
    }

}
