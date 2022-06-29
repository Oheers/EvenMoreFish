package com.oheers.fish.events;

import com.oheers.fish.EvenMoreFish;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderLoadEvent implements Listener {
    private final EvenMoreFish plugin;

    public ItemsAdderLoadEvent(EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemsLoad(ItemsAdderLoadDataEvent event) {
        plugin.getLogger().info("Detected that itemsadder has finished loading all items...");
        plugin.getLogger().info("Reloading EMF.");
        plugin.reload();
    }

}
