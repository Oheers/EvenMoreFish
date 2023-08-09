package com.oheers.fish.addons.impl;


import com.denizenscript.denizen.objects.ItemTag;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.addons.ItemAddon;
import org.bukkit.inventory.ItemStack;

public class DenizenItemAddon extends ItemAddon {
    @Override
    public String getPrefix() {
        return "denizens";
    }

    @Override
    public String getPluginName() {
        return "Denizen";
    }

    @Override
    public String getAuthor() {
        return "FireML";
    }

    @Override
    public ItemStack getItemStack(String id) {
        final ItemTag itemTag = ItemTag.valueOf(id, false);
        if (itemTag == null) {
            EvenMoreFish.logger.info(() -> String.format("Could not obtain denizen item %s", id));
            return null;
        }

        return itemTag.getItemStack();
    }
}
