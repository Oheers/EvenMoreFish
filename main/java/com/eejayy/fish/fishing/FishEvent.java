package com.eejayy.fish.fishing;

import com.eejayy.fish.EvenMoreFish;
import com.eejayy.fish.config.MainConfig;
import com.eejayy.fish.fishing.items.Fish;
import com.eejayy.fish.fishing.items.Rarities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.Plugin;

public class FishEvent implements Listener {



    @EventHandler
    public void onFish(PlayerFishEvent event) {

        if (MainConfig.enabled) {

            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                event.setCancelled(true);
                event.getHook().remove();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(event.getPlayer().getName() + " has fished a gigantic fish. Let's hear a round of applause for them.");
                }

                Fish fish = new Fish(Rarities.random(), event.getPlayer());

                Location location = event.getPlayer().getLocation();
                World world = location.getWorld();

                world.dropItem(location, fish.getItem());
            }

        }
    }
}
