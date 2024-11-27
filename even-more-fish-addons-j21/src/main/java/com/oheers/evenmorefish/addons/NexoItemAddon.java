package com.oheers.evenmorefish.addons;


import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import com.nexomc.nexo.items.ItemBuilder;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class NexoItemAddon extends ItemAddon implements Listener {

    private boolean nexoLoaded = false;
    
    @Override
    public String getPrefix() {
        return "nexo";
    }

    @Override
    public String getPluginName() {
        return "Nexo";
    }

    @Override
    public String getAuthor() {
        return "FireML";
    }

    @Override
    public ItemStack getItemStack(String id) {
        if (!nexoLoaded) {
            return null;
        }

        final ItemBuilder item = NexoItems.itemFromId(id);

        if (item == null) {
            getLogger().info(() -> String.format("Could not obtain Nexo item %s", id));
            return null;
        }
        return item.build();
    }

    @EventHandler
    public void onItemsLoad(NexoItemsLoadedEvent event) {
        getLogger().info("Detected that Nexo has finished loading all items...");
        getLogger().info("Reloading EMF.");
        this.nexoLoaded = true;

        EMFPlugin.getInstance().reload(null);
    }

}
