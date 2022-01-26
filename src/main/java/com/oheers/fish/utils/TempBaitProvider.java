package com.oheers.fish.utils;

import com.oheers.fish.FishUtils;
import com.oheers.fish.baits.Bait;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collections;

public class TempBaitProvider implements Listener {

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		try {
			if (event.getFrom().getChunk() != event.getTo().getChunk()) {
				Bait bait = new Bait("Rare Elixir");
				FishUtils.giveItems(Collections.singletonList(bait.create()), event.getPlayer());
			}
		} catch (NullPointerException ignored) {}
	}
}
