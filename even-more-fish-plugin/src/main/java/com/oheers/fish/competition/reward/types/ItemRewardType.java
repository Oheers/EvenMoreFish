package com.oheers.fish.competition.reward.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.reward.RewardType;
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
        Material material = Material.getMaterial(parsedItem[0]);
        if (material == null) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid material specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        ItemStack item;
        if (parsedItem.length == 1) {
            item = new ItemStack(material);
        } else {
            int quantity;
            try {
                quantity = Integer.parseInt(parsedItem[1]);
            } catch (NumberFormatException ex) {
                EvenMoreFish.getInstance().getLogger().warning("Invalid quantity specified for RewardType " + getIdentifier() + ": " + parsedItem[1]);
                return;
            }
            item = new ItemStack(material, quantity);
        }
        System.out.println(item);
        FishUtils.giveItems(Collections.singletonList(item), player);
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
