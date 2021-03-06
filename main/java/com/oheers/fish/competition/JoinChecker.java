package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinChecker implements Listener {

    // Gives the player the active fishing bar if there's a fishing event cracking off
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (EvenMoreFish.active != null) {
            EvenMoreFish.active.getBar().addPlayer(event.getPlayer());
        }
    }

    // Removes the player from the bar list if they leave the server
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (EvenMoreFish.active != null) {
            EvenMoreFish.active.getBar().removePlayer(event.getPlayer());
        }
    }
}
