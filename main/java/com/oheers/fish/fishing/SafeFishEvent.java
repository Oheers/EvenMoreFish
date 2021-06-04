package com.oheers.fish.fishing;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class SafeFishEvent implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onFish(PlayerFishEvent event) {
        FishingProcessor.process(event, true);
    }
}