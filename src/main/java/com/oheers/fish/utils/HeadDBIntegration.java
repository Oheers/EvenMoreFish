package com.oheers.fish.utils;

import com.oheers.fish.EvenMoreFish;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HeadDBIntegration implements Listener {

    @EventHandler
    public void onHDBLoad(DatabaseLoadEvent event) {
        EvenMoreFish.HDBapi = new HeadDatabaseAPI();
    }
}
