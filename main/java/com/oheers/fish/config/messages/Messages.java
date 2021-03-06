package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;

public class Messages {

    // hardcoded admin messages
    public static final String RELOADED = "&c[EvenMoreFish Admin] &rsuccessfully reloaded the plugin.";

    public static String fishCaught = EvenMoreFish.messageFile.getConfig().getString("fish-caught");

    public static String noPermission = EvenMoreFish.messageFile.getConfig().getString("no-permission");

    public static String bar_prefix = EvenMoreFish.messageFile.getConfig().getString("bossbar.prefix");
    public static String bar_hour = EvenMoreFish.messageFile.getConfig().getString("bossbar.hour");
    public static String bar_minute = EvenMoreFish.messageFile.getConfig().getString("bossbar.minute");
    public static String bar_second = EvenMoreFish.messageFile.getConfig().getString("bossbar.second");

}
