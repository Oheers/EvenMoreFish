package com.oheers.fish.api;

import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;

import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;

public class EMFAPI {
	private final EvenMoreFish plugin;

	public EMFAPI(EvenMoreFish plugin) {
		this.plugin = plugin;
	}

	public boolean isFish(ItemStack item) {
		return FishUtils.isFish(item);
	}

	public boolean isFish(Skull skull) {
		return FishUtils.isFish(skull);
	}

	public boolean isBait(ItemStack item) {
		return FishUtils.isBaitObject(item);
	}

	public Fish getFish(ItemStack item) {
		return FishUtils.getFish(item);
	}

	public Fish getFish(Skull skull, Player fisher) throws InvalidFishException {
		return FishUtils.getFish(skull, fisher);
	}

	public Optional<Fish> getFish(String rarity, String name) {
		for (Entry<Rarity, List<Fish>> e : EvenMoreFish.fishCollection.entrySet()) {
			if (e.getKey().getValue().equalsIgnoreCase(rarity)) {
				Optional<Fish> optFish = e.getValue().stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst();
				if (optFish.isPresent()) {
					return optFish;
				}
			}
		}
		return Optional.empty();
	}
}
