package com.oheers.fish.competition.rewardtypes.external;

import com.oheers.fish.Economy;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.economy.EconomyType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MoneyRewardType implements RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        Economy economy = EvenMoreFish.getInstance().getEconomy();
        int amount;
        try {
            amount = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        if (!economy.isEnabled()) {
            return;
        }
        EconomyType vault = economy.getEconomyType("Vault");
        if (vault == null) {
            return;
        }
        vault.deposit(player, amount, false);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "MONEY";
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
