package com.oheers.fish.economy;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class PlayerPointsEconomyType implements EconomyType {

    private PlayerPointsAPI economy = null;

    public PlayerPointsEconomyType() {
        EvenMoreFish emf = EvenMoreFish.getInstance();
        emf.getLogger().log(Level.INFO, "Economy attempting to hook into PlayerPoints.");
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            economy = PlayerPoints.getInstance().getAPI();
            emf.getLogger().log(Level.INFO, "Economy hooked into PlayerPoints.");
        }
    }

    @Override
    public String getIdentifier() {
        return "PlayerPoints";
    }

    @Override
    public double getMultiplier() {
        return MainConfig.getInstance().getEconomyMultiplier(this);
    }

    @Override
    public boolean deposit(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier) {
        if (!isAvailable()) {
            return false;
        }
        return economy.give(player.getUniqueId(), (int) prepareValue(amount, allowMultiplier));
    }

    @Override
    public boolean withdraw(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier) {
        if (!isAvailable()) {
            return false;
        }
        return economy.take(player.getUniqueId(), (int) prepareValue(amount, allowMultiplier));
    }

    @Override
    public boolean has(@NotNull OfflinePlayer player, double amount) {
        if (!isAvailable()) {
            return false;
        }
        return get(player) >= amount;
    }

    @Override
    public double get(@NotNull OfflinePlayer player) {
        if (!isAvailable()) {
            return 0;
        }
        return economy.look(player.getUniqueId());
    }

    @Override
    public double prepareValue(double value, boolean applyMultiplier) {
        if (applyMultiplier) {
            return Math.floor(value * getMultiplier());
        }
        return Math.floor(value);
    }

    @Override
    public @Nullable String formatWorth(double totalWorth, boolean applyMultiplier) {
        int worth = (int) prepareValue(totalWorth, applyMultiplier);
        if (worth <= 1) {
            return worth + " Player Point";
        } else {
            return worth + " Player Points";
        }
    }

    @Override
    public boolean isAvailable() {
        return (MainConfig.getInstance().isEconomyEnabled(this) && economy != null);
    }

}
