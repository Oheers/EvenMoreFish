package com.oheers.evenmorefish.addons;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.addons.ItemAddon;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.inventory.ItemStack;

public class HeadDatabaseItemAddon extends ItemAddon {
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
        final HeadDatabaseAPI api = new HeadDatabaseAPI();
        if(!api.isHead(id)) {
            getLogger().warning(() -> String.format("No such head with the id %s",id));
            return null;
        }

        return api.getItemHead(id);
    }
}
