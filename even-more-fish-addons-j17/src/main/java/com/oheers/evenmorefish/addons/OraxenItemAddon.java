package com.oheers.evenmorefish.addons;


import com.oheers.fish.api.addons.ItemAddon;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

public class OraxenItemAddon extends ItemAddon {
    @Override
    public String getPrefix() {
        return "oraxen";
    }

    @Override
    public String getPluginName() {
        return "Oraxen";
    }

    @Override
    public String getAuthor() {
        return "FireML";
    }

    @Override
    public ItemStack getItemStack(String id) {
        final ItemBuilder item = OraxenItems.getItemById(id);

        if (item == null) {
            getLogger().info(() -> String.format("Could not obtain oraxen item %s", id));
            return null;
        }
        return item.build();
    }

}
