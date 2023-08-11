package com.oheers.fish.api.addons;

import org.bukkit.inventory.ItemStack;

public abstract class ItemAddon implements Addon{

    /**
     * @param id id of the ItemStack
     * @return The ItemStack via the id
     */
    public abstract ItemStack getItemStack(final String id);


    @Override
    public final String toString() {
        return String.format("ItemAddon[prefix: %s, author: %s]",getPrefix(), getAuthor());
    }

}
