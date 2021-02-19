package com.eejayy.fish.fishing;

import com.eejayy.fish.EvenMoreFish;
import com.eejayy.fish.config.MainConfig;
import com.eejayy.fish.fishing.items.Fish;
import com.eejayy.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.ArrayList;
import java.util.List;

public class FishEvent implements Listener {



    @EventHandler
    public void onFish(PlayerFishEvent event) {

        if (MainConfig.enabled) {

            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {

                // Cancels the event then creates a fake catching 'animation'
                event.setCancelled(true);
                event.getHook().remove();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(event.getPlayer().getName() + " has fished a gigantic fish. Let's hear a round of applause for them.");
                }

                Fish fish = new Fish(random(), event.getPlayer(), 5.0f, 27.0f);

                /* Drops the item rather than giving it straight to the player as a slap-dash way of checking the inventory
                 isn't full */
                Location location = event.getPlayer().getLocation();
                World world = location.getWorld();

                world.dropItem(location, fish.getItem());
            }

        }
    }

    private Rarity random() {
        // Loads all the rarities
        List<Rarity> rarities = new ArrayList<>(EvenMoreFish.fish.keySet());

        double totalWeight = 0;

        // Weighted random logic (nabbed from stackoverflow)
        for (Rarity r : rarities) {
            totalWeight += r.getWeight();
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < rarities.size() - 1; ++idx) {
            r -= rarities.get(idx).getWeight();
            if (r <= 0.0) break;
        }

        return rarities.get(idx);
    }
}
