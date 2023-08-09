package com.oheers.fish.events;

import com.oheers.fish.EvenMoreFish;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OraxenLoadEvent implements Listener {

    private final EvenMoreFish plugin;

    public OraxenLoadEvent(EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onOraxenLoad(OraxenItemsLoadedEvent e) {
        plugin.getLogger().info("Detected that Oraxen has finished loading all items...");
        plugin.getLogger().info("Reloading EMF.");
        EvenMoreFish.oraxenLoaded = true;
        plugin.reload();
    }
}
