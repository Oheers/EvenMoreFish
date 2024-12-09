package com.oheers.fish.economy;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class GriefPreventionEconomyType implements EconomyType {

    private GriefPrevention economy = null;

    public GriefPreventionEconomyType() {
        EvenMoreFish emf = EvenMoreFish.getInstance();
        emf.getLogger().log(Level.INFO, "Economy attempting to hook into GriefPrevention.");
        if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            economy = GriefPrevention.instance;
            emf.getLogger().log(Level.INFO, "Economy hooked into GriefPrevention.");
        }
    }

    @Override
    public String getIdentifier() {
        return "GriefPrevention";
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
        PlayerData data = economy.dataStore.getPlayerData(player.getUniqueId());
        data.setBonusClaimBlocks(data.getBonusClaimBlocks() + (int) prepareValue(amount, allowMultiplier));
        return true;
    }

    @Override
    public boolean withdraw(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier) {
        if (!isAvailable()) {
            return false;
        }
        PlayerData data = economy.dataStore.getPlayerData(player.getUniqueId());
        int total = data.getBonusClaimBlocks();
        int finalTotal = total - (int) prepareValue(amount, allowMultiplier);
        if (finalTotal < 0) {
            return false;
        }
        data.setBonusClaimBlocks(finalTotal);
        return true;
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
        return economy.dataStore.getPlayerData(player.getUniqueId()).getBonusClaimBlocks();
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
        if (!isAvailable()) {
            return null;
        }
        int worth = (int) prepareValue(totalWorth, applyMultiplier);
        String display = MainConfig.getInstance().getEconomyDisplay(this);
        if (display == null) {
            display = "{amount} Claim Block(s)";
        }
        AbstractMessage message = EvenMoreFish.getAdapter().createMessage(display);
        message.setVariable("{amount}", String.valueOf(worth));
        return message.getLegacyMessage();
    }

    @Override
    public boolean isAvailable() {
        return (MainConfig.getInstance().isEconomyEnabled(this) && economy != null);
    }

}
