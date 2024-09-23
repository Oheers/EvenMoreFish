package com.oheers.fish.api.requirement;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Designed for plugins to re-register their requirement types if the plugin is reloaded with (for example) PlugManX
 */
public class EMFRequirementsLoadEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

}
