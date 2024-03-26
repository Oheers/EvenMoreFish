package com.oheers.fish.competition.reward.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class HealthRewardType implements RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        double rewardHealth;
        try {
            rewardHealth = Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth;
        if (attribute == null) {
            maxHealth = 20.0D;
        } else {
            maxHealth = attribute.getValue();
        }
        double finalHealth = player.getHealth() + rewardHealth;
        System.out.println(finalHealth);
        if (finalHealth > maxHealth) {
            player.setHealth(maxHealth);
        } else if (finalHealth < 0)  {
            player.setHealth(0);
        } else {
            player.setHealth(finalHealth);
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "HEALTH";
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
