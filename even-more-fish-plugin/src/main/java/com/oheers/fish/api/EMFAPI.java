package com.oheers.fish.api;

import com.oheers.fish.FishUtils;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EMFAPI {

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

    public @Nullable Fish getFish(String rarityName, String fishName) {
        return FishManager.getInstance().getFish(rarityName, fishName);
    }

}
