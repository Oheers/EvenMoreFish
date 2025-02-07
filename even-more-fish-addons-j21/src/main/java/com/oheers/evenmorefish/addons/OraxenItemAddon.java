package com.oheers.evenmorefish.addons;


import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class OraxenItemAddon extends ItemAddon implements Listener {
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
        if (!OraxenItems.exists(id)) {
            getLogger().warning(() -> "Oraxen item with id %s doesn't exist.".formatted(id));
            return null;
        }

        final ItemBuilder item = OraxenItems.getItemById(id);

        if (item == null) {
            getLogger().info(() -> "Could not obtain Oraxen item %s".formatted(id));
            return null;
        }

        return item.build();
    }

    @EventHandler
    public void onItemsLoad(OraxenItemsLoadedEvent event) {
        getLogger().info("Detected that Oraxen has finished loading all items...");
        getLogger().info("Reloading EMF.");

        EMFPlugin.getInstance().reload(null);
    }
}
