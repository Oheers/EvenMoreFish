package com.oheers.fish.competition.rewardtypes;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ItemRewardType implements RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        String[] parsedItem = value.split(",");
        Material material = Material.getMaterial(parsedItem[0].toUpperCase());
        if (material == null) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid material specified for RewardType " + getIdentifier() + ": " + parsedItem[0]);
            return;
        }
        ItemStack item = new ItemStack(material);
        int quantity = 1;
        if (parsedItem.length > 1) {
            try {
                quantity = Integer.parseInt(parsedItem[1]);
            } catch (NumberFormatException ex) {
                EvenMoreFish.getInstance().getLogger().warning("Invalid quantity specified for RewardType " + getIdentifier() + ": " + parsedItem[1]);
                return;
            }
        }
        for (int i = 0; i < quantity; ++i) {
            FishUtils.giveItem(item, player);
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ITEM";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NotNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
