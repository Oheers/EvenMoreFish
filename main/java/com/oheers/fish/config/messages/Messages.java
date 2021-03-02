package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;

public class Messages {

    // hardcoded admin messages
    public static final String RELOADED = "&c[EvenMoreFish Admin] &rsuccessfully reloaded the plugin.";

    public static String fishCaught = EvenMoreFish.messageFile.getConfig().getString("fish-caught");

    public static String noPermission = EvenMoreFish.messageFile.getConfig().getString("no-permission");

}
