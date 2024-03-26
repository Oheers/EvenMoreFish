package com.oheers.fish.competition.reward.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.reward.RewardType;
import com.oheers.fish.config.messages.OldMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MessageRewardType implements RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        player.sendMessage(new OldMessage().setMSG(value).setReceiver(player).toString());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "MESSAGE";
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
