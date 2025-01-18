package com.oheers.evenmorefish.addons;


import com.denizenscript.denizen.objects.ItemTag;
import com.oheers.fish.api.addons.ItemAddon;
import org.bukkit.inventory.ItemStack;

public class DenizenItemAddon extends ItemAddon {

    @Override
    public String getPrefix() {
        return "denizen";
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
            getLogger().info(() -> String.format("Could not obtain denizen item %s", id));
            return null;
        }

        return itemTag.getItemStack();
    }

}
