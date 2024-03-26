package com.oheers.fish.competition.rewardtypes;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EffectRewardType implements RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        String[] parsedEffect = value.split(",");
        if (parsedEffect.length < 3) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid effect specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        // Adds a potion effect in accordance to the config.yml "EFFECT:" value
        EvenMoreFish.getScheduler().runTask(player, () -> player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(parsedEffect[0])), Integer.parseInt(parsedEffect[2]) * 20, Integer.parseInt(parsedEffect[1]))));
    }

    @Override
    public @NotNull String getIdentifier() {
        return "EFFECT";
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
