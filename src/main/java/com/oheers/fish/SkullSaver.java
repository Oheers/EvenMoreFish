package com.oheers.fish;

import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.selling.WorthNBT;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class SkullSaver implements Listener {

	// EventPriority.HIGHEST makes this run last so it can listen to the cancels of protection plugins like Towny
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBreak(BlockBreakEvent event) {
		if(event.isCancelled()) return;
		if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
		Block block = event.getBlock();
		if(block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
			if(block.getDrops().size() == 0) return;

			BlockState state = event.getBlock().getState();
			Skull sm = (Skull) state;
			if(FishUtils.isFish(sm)) {
				ItemStack stack = block.getDrops().iterator().next().clone();
				event.setCancelled(true);

				try {
					Fish f = FishUtils.getFish(sm, event.getPlayer());

					stack.setItemMeta(f.give().getItemMeta());
					block.setType(Material.AIR);
					block.getWorld().dropItem(block.getLocation(), stack);
					block.getWorld().playSound(block.getLocation(), Sound.BLOCK_BONE_BLOCK_BREAK, 1, 1);
				} catch (InvalidFishException exception) {
					EvenMoreFish.logger.log(Level.SEVERE, "Error fetching fish from config at location:" +
							" x:" + event.getBlock().getLocation().getBlockX() +
							" y:" + event.getBlock().getLocation().getBlockY() +
							" z:" + event.getBlock().getLocation().getBlockZ() +
							" world:" + event.getBlock().getLocation().getBlock().getWorld().getName());
				}
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlace(BlockPlaceEvent event) {

		if (event.isCancelled()) {
			return;
		}

		if (EvenMoreFish.mainConfig.blockPlacingHeads()) {
			event.setCancelled(true);
			new Message(ConfigMessage.FISH_CANT_BE_PLACED).broadcast(event.getPlayer(), true, false);
			return;
		}

		Block block = event.getBlock();
		ItemStack stack = event.getItemInHand();

		if(stack.getAmount() == 0 || !stack.hasItemMeta()) {
			return;
		}

		if(FishUtils.isFish(stack)) {
			if (block.getState() instanceof Skull) {
				Fish f = FishUtils.getFish(stack);
				BlockState state = block.getState();
				Skull sm = (Skull) state;

				WorthNBT.setNBT(sm, f.getLength(), f.getFisherman(), f.getRarity().getValue(), f.getName());

				sm.update();
			} else {
				event.setCancelled(true);
			}
		}
	}
}
