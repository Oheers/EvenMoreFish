package com.oheers.fish.events;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class FishEatEvent implements Listener {

    private static final FishEatEvent eatEvent = new FishEatEvent();

    private FishEatEvent() {

    }

    public static FishEatEvent getInstance() {
        return eatEvent;
    }

    @EventHandler
    public void onEatItem(final PlayerItemConsumeEvent event) {
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
}
