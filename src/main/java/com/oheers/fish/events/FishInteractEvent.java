package com.oheers.fish.events;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FishInteractEvent implements Listener {

    private static final FishInteractEvent interactEvent = new FishInteractEvent();

    private FishInteractEvent() {

    }

    public static FishInteractEvent getInstance() {
        return interactEvent;
    }

    @EventHandler
    public void interactEvent(PlayerInteractEvent event) {
        // If there is no item, or the player is sneaking (to place the head), the item isn't actually a fish, or the event fired for the off hand don't do anything
        if (event.getItem() == null || event.getPlayer().isSneaking() || !FishUtils.isFish(event.getItem()) || event.getHand() == EquipmentSlot.OFF_HAND
                || !event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }
        // Creates a replica of the fish we can use
        Fish fish = FishUtils.getFish(event.getItem());
        if (fish != null) {
            if (fish.hasIntRewards()) {
                // Cancel the interact event
                event.setCancelled(true);
                // Take one item from the player's event hand itemstack so we know that it's gone
                ItemStack itemInHand = event.getItem();
                event.getPlayer().getInventory().getItemInMainHand().setAmount(itemInHand.getAmount() - 1);
                // Runs through each eat-event
                for (Reward r : fish.getActionRewards()) {
                    r.run(event.getPlayer(), null);
                }
            }
        }
    }
}
