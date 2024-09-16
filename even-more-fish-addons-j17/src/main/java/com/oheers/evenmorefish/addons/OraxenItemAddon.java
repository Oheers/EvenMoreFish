package com.oheers.evenmorefish.addons;


import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class OraxenItemAddon extends ItemAddon implements Listener {
    private boolean oraxenLoaded = false;
    
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
        if (!oraxenLoaded) {
            return null;
        }
        
        final ItemBuilder item = OraxenItems.getItemById(id);

        if (item == null) {
            getLogger().info(() -> String.format("Could not obtain oraxen item %s", id));
            return null;
        }
        return item.build();
    }

    @EventHandler
    public void onItemsLoad(OraxenItemsLoadedEvent event) {
        getLogger().info("Detected that oraxen has finished loading all items...");
        getLogger().info("Reloading EMF.");
        this.oraxenLoaded = true;

        ((EMFPlugin) Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("EvenMoreFish"))).reload(null);
    }

}
