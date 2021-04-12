package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;

public class Messages {

    public static final String PREFIX_STD = EvenMoreFish.messageFile.getConfig().getString("prefix-regular") + "[EvenMoreFish] &r";
    private static final String PREFIX_ADM = EvenMoreFish.messageFile.getConfig().getString("prefix-admin") + "[EvenMoreFish] &r";
    private static final String PREFIX_ERR = EvenMoreFish.messageFile.getConfig().getString("prefix-error") + "[EvenMoreFish] &r";

    public static final String RELOADED = PREFIX_ADM + "successfully reloaded the plugin.";

    public static final String FISH_CAUGHT = EvenMoreFish.messageFile.getConfig().getString("fish-caught");

    public static final String NO_PERMISSION = PREFIX_ERR + EvenMoreFish.messageFile.getConfig().getString("no-permission");
    public static final String NOT_INT = PREFIX_ERR + "Please provide an integer value.";
    public static final String COMPETITION_RUNNING = PREFIX_ERR + "There's a competition running.";
    public static final String COMPETITION_NOT_RUNNING = PREFIX_ERR + "There's no competition running.";

    public static final String BAR_PREFIX = EvenMoreFish.messageFile.getConfig().getString("bossbar.prefix");
    public static final String BAR_HOUR = EvenMoreFish.messageFile.getConfig().getString("bossbar.hour");
    public static final String BAR_MINUTE = EvenMoreFish.messageFile.getConfig().getString("bossbar.minute");
    public static final String BAR_SECOND = EvenMoreFish.messageFile.getConfig().getString("bossbar.second");

    public static final String EMF_HELP = PREFIX_STD + EvenMoreFish.messageFile.getConfig().getString("help");
    public static final String LEADERBOARD = PREFIX_STD + EvenMoreFish.messageFile.getConfig().getString("leaderboard");

    public static final int LEADERBOARD_COUNT = EvenMoreFish.messageFile.getConfig().getInt("leaderboard-count");

    public static final String COMPETITION_START = PREFIX_STD + EvenMoreFish.messageFile.getConfig().getString("contest-start");
    public static final String COMPETITION_END = PREFIX_STD + EvenMoreFish.messageFile.getConfig().getString("contest-end");
    public static final String NO_WINNERS = PREFIX_STD + EvenMoreFish.messageFile.getConfig().getString("no-winners");
    public static final String NO_FISH = PREFIX_STD + EvenMoreFish.messageFile.getConfig().getString("no-record");

    public static final String WORTH_GUI_NAME = EvenMoreFish.messageFile.getConfig().getString("worth-gui-name");
    public static final String SELL_MESSAGE = PREFIX_STD + EvenMoreFish.messageFile.getConfig().getString("fish-sale");

}
