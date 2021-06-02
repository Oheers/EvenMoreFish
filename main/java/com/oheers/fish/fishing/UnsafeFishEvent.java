package com.oheers.fish.fishing;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class UnsafeFishEvent implements Listener {

    // This is highly likely to break plugins: do not enable this feature in the config unless you're absolutely certain you know what you're
    // doing. "MONITOR" should never be used when the outcome of an event is being modified.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFish(PlayerFishEvent event) {
        FishingProcessor.process(event, false);
    }
}