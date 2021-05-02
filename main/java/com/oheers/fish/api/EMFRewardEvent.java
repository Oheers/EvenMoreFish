package com.oheers.fish.api;

import com.oheers.fish.competition.reward.Reward;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EMFRewardEvent extends Event {

    Reward reward;
    Player player;

    private static final HandlerList handlers = new HandlerList();

    public EMFRewardEvent(Reward reward, Player player) {
        this.reward = reward;
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
     * @returns A list of Reward objects that will be run on the player.
     */
    public Reward getReward() {
        return this.reward;
    }

    /**
     * @returns The player that each Reward object will be run on
     */
    public Player getPlayer() {
        return this.player;
    }
}