package com.oheers.evenmorefish.addons;

import com.oheers.fish.api.addons.ItemAddon;
import com.willfp.ecoitems.items.EcoItem;
import com.willfp.ecoitems.items.EcoItems;
import org.bukkit.inventory.ItemStack;

public class EcoItemsItemAddon extends ItemAddon {
    @Override
    public String getPrefix() {
        return "ecoitems";
    }

    @Override
    public String getPluginName() {
        return "EcoItems";
    }

    @Override
    public String getAuthor() {
        return "FireML";
    }

    @Override
    public ItemStack getItemStack(String id) {
        final EcoItem item = EcoItems.INSTANCE.getByID(id);

        if (item == null) {
            getLogger().info(() -> String.format("Could not obtain EcoItems item %s", id));
            return null;
        }

        return item.getItemStack();
    }

}
