package com.oheers.fish.events;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class FishInteractEvent implements Listener {

    private final EvenMoreFish plugin;

    public FishInteractEvent(final EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void interactEvent(PlayerInteractEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getItem() != null) {
                    // Allows players to place fishing heads if they're sneaking
                    if (!event.getPlayer().isSneaking()) {
                        // Checks if the eaten item is a fish
                        if (FishUtils.isFish(event.getItem())) {
                            // Creates a replica of the fish we can use
                            Fish fish = FishUtils.getFish(event.getItem());
                            if (fish.hasIntRewards()) {

                                event.setCancelled(true);
                                // Runs through each eat-event
                                for (Reward r : fish.getActionRewards()) {
                                    r.run(event.getPlayer());
                                }

                                // ux seems a bit weird when we allow offhand iteraction.
                                ItemStack mainh = event.getPlayer().getInventory().getItemInMainHand();
                                if (FishUtils.isFish(mainh)) {
                                    // adds a -1 amount version of the itemstack to the player's inventory
                                    mainh.setAmount(mainh.getAmount() - 1);
                                    event.getPlayer().getInventory().setItemInMainHand(mainh);
                                }
                            }
                        }
                    }
                }

            }
            // Running it async since it doesn't need to be on the main thread
        }.runTaskAsynchronously(this.plugin);
    }
}
