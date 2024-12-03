package com.oheers.fish.economy;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public interface EconomyType {

    String getIdentifier();

    double getMultiplier();

    boolean deposit(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier);

    boolean withdraw(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier);

    boolean has(@NotNull OfflinePlayer player, double amount);

    double get(@NotNull OfflinePlayer player);

    /**
     * Prepares a double for use with this economy type.
     * @param value The value to prepare.
     * @param applyMultiplier Should we apply the multiplier?
     * @return A prepared double for use with this economy type.
     */
    double prepareValue(double value, boolean applyMultiplier);

    /**
     * Creates a String to represent this value.
     * @param totalWorth The value to represent.
     * @param applyMultiplier Should the multiplier be applied to the value?
     * @return A String to represent this value.
     */
    String formatWorth(double totalWorth, boolean applyMultiplier);

    boolean isAvailable();

}
