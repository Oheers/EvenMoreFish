package com.oheers.fish.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EMFRewardFormattingEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private String guiText;
    private boolean changing;

    public EMFRewardFormattingEvent(String reward) {
        this.guiText = reward;
        this.changing = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public String getReward() {
        return guiText;
    }

    public void setReward(String outputtingText) {
        this.changing = true;
        this.guiText = outputtingText;
    }

    public boolean isChanging() {
        return changing;
    }
}
