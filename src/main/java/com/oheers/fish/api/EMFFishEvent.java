package com.oheers.fish.api;

import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EMFFishEvent extends Event {

    Fish fish;
    Player player;

    private static final HandlerList handlers = new HandlerList();

    public EMFFishEvent(Fish fish, Player player) {
        this.fish = fish;
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * @return The fish that the player is receiving
     */
    public Fish getFish() {
        return fish;
    }

    /**
     * @return The player that has fished the fish
     */
    public Player getPlayer() {
        return player;
    }
}