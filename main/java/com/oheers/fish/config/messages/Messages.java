package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class Messages {

    // hardcoded admin messages
    public static final String RELOADED = "&c[EvenMoreFish Admin] &rsuccessfully reloaded the plugin.";

    public static String fishCaught = EvenMoreFish.messageFile.getConfig().getString("fish-caught");

    public static String noPermission = EvenMoreFish.messageFile.getConfig().getString("no-permission");

}
