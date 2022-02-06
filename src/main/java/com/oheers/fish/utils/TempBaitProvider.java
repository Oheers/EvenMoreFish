package com.oheers.fish.utils;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collections;
import java.util.Random;

public class TempBaitProvider implements Listener {

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		try {
			if (event.getFrom().getChunk() != event.getTo().getChunk()) {
				String[] knownBaits = { "Shrimp", "Rare Elixir", "Stringy Worms" };
				FishUtils.giveItems(Collections.singletonList(EvenMoreFish.baits.get(knownBaits[new Random().nextInt(3)]).create()), event.getPlayer());
			}
		} catch (NullPointerException error) {
			error.printStackTrace();
		}
	}
}
