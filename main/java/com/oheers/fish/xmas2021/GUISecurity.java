package com.oheers.fish.xmas2021;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUISecurity implements Listener {

	@EventHandler
	public void guiInteract(InventoryClickEvent event) {

		if (Xmas2021.getFocusedPlayers().contains(event.getWhoClicked().getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void guiClose(InventoryCloseEvent event) {

		if (Xmas2021.getFocusedPlayers().contains(event.getPlayer().getUniqueId())) {
			if (event.getPlayer() instanceof Player) {
				Xmas2021.unfocusPlayer((Player) event.getPlayer());
			}
		}
	}
}