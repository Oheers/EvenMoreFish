package com.oheers.fish.addons.impl;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.addons.ItemAddon;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.inventory.ItemStack;

public class HeadDatabaseItemAddon extends ItemAddon {
    private final HeadDatabaseAPI api;

    public HeadDatabaseItemAddon() {
        this.api = new HeadDatabaseAPI();
    }

    @Override
    public String getPrefix() {
        return "headdb";
    }

    @Override
    public String getPluginName() {
        return "HeadDatabase";
    }

    @Override
    public String getAuthor() {
        return "EvenMoreFish";
    }

    @Override
    public ItemStack getItemStack(String id) {
        if(!api.isHead(id)) {
            EvenMoreFish.logger.warning(() -> String.format("No such head with the id %s",id));
            return null;
        }

        return api.getItemHead(id);
    }
}
