package com.oheers.evenmorefish.addons;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.addons.ItemAddon;
import org.bukkit.Material;
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
            EvenMoreFish.logger.info(() -> String.format("Could not obtain oraxen item %s", id));
            return new ItemStack(Material.COD);
        }
        return item.build();
    }
}
