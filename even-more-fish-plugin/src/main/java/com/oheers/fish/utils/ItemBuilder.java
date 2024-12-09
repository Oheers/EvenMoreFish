package com.oheers.fish.utils;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.adapter.AbstractMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    public ItemBuilder withMaterial(@NotNull Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder withMaterial(@NotNull String materialName, @NotNull Material defaultMaterial) {
        this.material = ItemUtils.getMaterial(materialName, defaultMaterial);
        return this;
    }

    public ItemBuilder withDisplay(@NotNull String display) {
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
            meta.setDisplayName(EvenMoreFish.getAdapter().createMessage(this.display).getLegacyMessage());
        }
        if (!this.lore.isEmpty()) {
            meta.setLore(EvenMoreFish.getAdapter().createMessage(this.lore).getLegacyListMessage());
        }
        stack.setItemMeta(meta);
        if (this.glowing) {
            ItemUtils.glowify(stack);
        }
        return stack;
    }

}
