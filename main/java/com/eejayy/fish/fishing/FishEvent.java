package com.eejayy.fish.fishing;

import com.eejayy.fish.fishing.items.Fish;
import com.eejayy.fish.fishing.items.Rarities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class FishEvent implements Listener {

    @EventHandler
    public void onFish(PlayerFishEvent event) {

        if (event.getCaught() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(event.getPlayer().getName() + " has fished a gigantic fish. Let's hear a round of applause for them.");
            }

            Fish fish = new Fish(Rarities.random(), event.getPlayer());

            event.getPlayer().getInventory().addItem(fish.getItem());
        }
    }
}
