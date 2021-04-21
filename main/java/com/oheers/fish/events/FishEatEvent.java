package com.oheers.fish.events;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FishEatEvent implements Listener {

    private final EvenMoreFish plugin;

    public FishEatEvent(final EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEatItem(final PlayerItemConsumeEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Checks if the eaten item is a fish
                if (FishUtils.isFish(event.getItem())) {
                    // Creates a replica of the fish we can use
                    Fish fish = FishUtils.getFish(event.getItem());
                    if (fish.hasEatRewards()) {
                        // Runs through each eat-event
                        for (Reward r : fish.getActionRewards()) {
                            r.run(event.getPlayer());
                        }
                    }
                }
            }
        // Running it async since it doesn't need to be on the main thread
        }.runTaskAsynchronously(this.plugin);
    }
}
