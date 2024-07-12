package com.oheers.fish.api.plugin;


import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public interface EMFPlugin {

    void reload(@Nullable CommandSender sender);

    static Logger getLogger() {
        return Logger.getLogger("EvenMoreFish");
    }

}
