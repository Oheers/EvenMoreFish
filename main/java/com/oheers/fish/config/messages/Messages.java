package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;

public class Messages {

    public static final String prefix_std = EvenMoreFish.messageFile.getConfig().getString("prefix-regular") + "[EvenMoreFish] &r";
    private static final String prefix_adm = EvenMoreFish.messageFile.getConfig().getString("prefix-admin") + "[EvenMoreFish] &r";
    private static final String prefix_err = EvenMoreFish.messageFile.getConfig().getString("prefix-error") + "[EvenMoreFish] &r";

    public static final String RELOADED = prefix_adm + "successfully reloaded the plugin.";

    public static final String fishCaught = EvenMoreFish.messageFile.getConfig().getString("fish-caught");

    public static final String noPermission = prefix_err + EvenMoreFish.messageFile.getConfig().getString("no-permission");
    public static final String notInt = prefix_err + "Please provide an integer value.";
    public static final String competitionRunning = prefix_err + "There's a competition running.";

    public static final String bar_prefix = EvenMoreFish.messageFile.getConfig().getString("bossbar.prefix");
    public static final String bar_hour = EvenMoreFish.messageFile.getConfig().getString("bossbar.hour");
    public static final String bar_minute = EvenMoreFish.messageFile.getConfig().getString("bossbar.minute");
    public static final String bar_second = EvenMoreFish.messageFile.getConfig().getString("bossbar.second");

    public static final String emf_help = prefix_std + EvenMoreFish.messageFile.getConfig().getString("help");

    public static final String competitionStart = prefix_std + EvenMoreFish.messageFile.getConfig().getString("contest-start");

}
