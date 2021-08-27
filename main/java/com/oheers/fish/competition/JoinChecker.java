package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinChecker implements Listener {

    // Gives the player the active fishing bar if there's a fishing event cracking off
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Competition.isActive()) {
            EvenMoreFish.active.getStatusBar().addPlayer(event.getPlayer());
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EvenMoreFish.getPlugin(EvenMoreFish.class),
            new Runnable() {
                @Override
                public void run() {
                    event.getPlayer().sendMessage(EvenMoreFish.active.getStartMessage());
                }
            }, 20*3);
        }
    }

    // Removes the player from the bar list if they leave the server
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (Competition.isActive()) {
            EvenMoreFish.active.getStatusBar().removePlayer(event.getPlayer());
        }
    }
}
