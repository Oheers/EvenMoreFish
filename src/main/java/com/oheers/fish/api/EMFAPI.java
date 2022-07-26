package com.oheers.fish.api;

import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;

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
}
