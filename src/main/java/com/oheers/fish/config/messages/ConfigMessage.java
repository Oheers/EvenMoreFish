package com.oheers.fish.config.messages;

import java.util.Arrays;
import java.util.List;

public enum ConfigMessage {

    ADMIN_CANT_BE_CONSOLE("admin.cannot-run-on-console", "&rCommand cannot be run from console.", PrefixType.ERROR, false, true),
    ADMIN_GIVE_PLAYER_BAIT("admin.given-player-bait", "&rYou have given {player} a {bait}.", PrefixType.ADMIN, true, true),
    ADMIN_GIVE_PLAYER_FISH("admin.given-player-fish", "&rYou have given {player} a {fish}.", PrefixType.ADMIN, true, true),
    ADMIN_OPEN_FISH_SHOP("admin.open-fish-shop", "&rOpened a shop inventory for {player}.", PrefixType.ADMIN, true, true),
    ADMIN_NO_BAIT_SPECIFIED("admin.no-bait-specified", "&rYou must specify a bait name.", PrefixType.ERROR, false, true),
    ADMIN_NOT_HOLDING_ROD("admin.must-be-holding-rod", "&rYou need to be holding a fishing rod to run that command.", PrefixType.ERROR, false, false),
    ADMIN_NUMBER_FORMAT_ERROR("admin.number-format-error", "&r{amount} is not a valid number.", PrefixType.ERROR, false, true),
    ADMIN_NUMBER_RANGE_ERROR("admin.number-range-error", "&r{amount} is not a number between 1-64.", PrefixType.ERROR, false, true),
    ADMIN_UNKNOWN_PLAYER("admin.player-not-found", "&r{player} could not be found.", PrefixType.ERROR, false, true),
    ADMIN_UPDATE_AVAILABLE("admin.update-available", "&rThere is an update available: " + "https://www.spigotmc.org/resources/evenmorefish.91310/updates", PrefixType.ADMIN, false, false),

    BAITS_CLEARED("admin.all-baits-cleared", "&rYou have removed all {amount} baits from your fishing rod.", PrefixType.ADMIN, true, false),
    BAIT_CAUGHT("bait-catch", "&r&l{player} &rhas caught a {bait_theme}&l{bait} &rbait!", PrefixType.NONE, true, false),
    BAIT_USED("bait-use", "&rYou have used one of your rod's {bait_theme}&l{bait} &rbait.", PrefixType.DEFAULT, true, false),
    BAIT_WRONG_GAMEMODE("bait-survival-limited", "&rYou must be in &nsurvival&r to apply baits to fishing rods.", PrefixType.ERROR, false, false),
    BAITS_MAXED("max-baits-reached", "&rYou have reached the maximum number of types of baits for this fishing rod.", PrefixType.DEFAULT, false, true),
    BAITS_MAXED_ON_ROD("max-baits-reached", "&rYou have reached the maximum number of {bait_theme}{bait} &rbait that can be applied to one rod.", PrefixType.ERROR, false, false),

    BAR_SECOND("bossbar.second", "s", PrefixType.NONE, true, false),
    BAR_MINUTE("bossbar.minute", "m", PrefixType.NONE, true, false),
    BAR_HOUR("bossbar.hour", "h", PrefixType.NONE, true, false),
    BAR_REMAINING("bossbar.remaining", "left", PrefixType.NONE, true, false),

    COMPETITION_ALREADY_RUNNING("admin.competition-already-running", "&rThere's already a competition running.", PrefixType.ADMIN, false, true),

    COMPETITION_END("contest-end", "&rThe fishing contest has ended.", PrefixType.DEFAULT, false, false),
    COMPETITION_JOIN("contest-join", "&rA fishing contest for {type} is going on.", PrefixType.DEFAULT, true, false),
    COMPETITION_START("contest-start", "&rA fishing contest for {type} has started.", PrefixType.DEFAULT, false, false),

    COMPETITION_TYPE_LARGEST("competition-types.largest", "the largest fish", PrefixType.NONE, true, false),
    COMPETITION_TYPE_LARGEST_TOTAL("competition-types.largest-total", "the largest total fish length", PrefixType.NONE, true, false),
    COMPETITION_TYPE_MOST("competition-types.most", "the most fish", PrefixType.NONE, true, false),
    COMPETITION_TYPE_SPECIFIC("competition-types.specific", "{amount} {rarity_colour}&l{rarity} {rarity_colour}{fish}&r", PrefixType.NONE, true, false),
    COMPETITION_TYPE_SPECIFIC_RARITY("competition-types.specific-rarity", "{amount} {rarity_colour}&l{rarity}&r fish", PrefixType.NONE, true, false),

    COMPETITION_SINGLE_WINNER("single-winner", "&r{player} has won the competition for {type}. Congratulations!", PrefixType.DEFAULT, true, true),

    ECONOMY_DISABLED("admin.economy-disabled", "&rEvenMoreFish's economy features are disabled.", PrefixType.ERROR, false, false),

    FISH_CANT_BE_PLACED("place-fish-blocked", "&rYou cannot place this fish.", PrefixType.ERROR, true, true),
    FISH_CAUGHT("fish-caught", "&r&l{player} &rhas fished a {rarity_colour}{length}cm &l{rarity} {rarity_colour}{fish}!", PrefixType.NONE, true, false),
    FISH_LENGTHLESS_CAUGHT("lengthless-fish-caught", "&r&l{player} &rhas fished a {rarity_colour}&l{rarity} {rarity_colour}{fish}!", PrefixType.NONE, true, false),
    FISH_LORE("fish-lore", Arrays.asList(
            "{fisherman_lore}",
            "{length_lore}",
            "",
            "{fish_lore}",
            "{rarity_colour}&l{rarity}"
    ), PrefixType.NONE, false, false),
    FISHERMAN_LORE("fisherman-lore", Arrays.asList(
            "&fCaught by {player}"
    ), PrefixType.NONE, false, false),
    LENGTH_LORE("length-lore", Arrays.asList(
            "&fMeasures {length}cm"
    ), PrefixType.NONE, false, false),
    FISH_SALE("fish-sale", "&rYou've sold &a{amount} &ffish for &a${sell-price}&f.", PrefixType.DEFAULT, true, true),

    HELP_GENERAL("help-general", Arrays.asList(
            "&f&m &#f1ffed&m &#e2ffdb&m &#d3ffc9&m &#c3ffb7&m &#b2ffa5&m &#9fff92&m &#8bff7f&m &#73ff6b&m &a&m &f &a&lEvenMoreFish &a&m &#73ff6b&m&m &#8bff7f&m &#9fff92&m &#b2ffa5&m &#c3ffb7&m &#d3ffc9&m &#e2ffdb&m &#f1ffed&m &f&m &f",
            "&f/emf top - Shows an ongoing competition's leaderboard.",
            "&f/emf help - Shows you this page.",
            "&f/emf shop - Opens a shop to sell your fish.",
            "&f/emf toggle - Toggles whether or not you receive custom fish.",
            "&f/emf admin - Admin command help page."
    ), PrefixType.DEFAULT, false, true),
    HELP_ADMIN("help-admin", Arrays.asList(
            "&f&m &#ffedeb&m &#ffdcd7&m &#ffcac3&m &#ffb8b0&m &#ffa69d&m &#ff948a&m &#ff8078&m &#ff6c66&m &c&m &f &c&lEvenMoreFish &c&m &#ff6c66&m&m &#ff8078&m &#ff948a&m &#ffa69d&m &#ffb8b0&m &#ffcac3&m &#ffdcd7&m &#ffedeb&m &f&m &f",
            "&f/emf admin competition <start/end> <duration> <type> - Starts or stops a competition",
            "&f/emf admin clearbait - Removes all applied baits from a fishing rod.",
            "&f/emf admin reload - Reloads the plugin's config files",
            "&f/emf admin version - Displays plugin information."
    ), PrefixType.ADMIN, false, true),
    HELP_COMPETITION("help-competition", Arrays.asList(
            "&f&m &#ffedeb&m &#ffdcd7&m &#ffcac3&m &#ffb8b0&m &#ffa69d&m &#ff948a&m &#ff8078&m &#ff6c66&m &c&m &f &c&lEvenMoreFish &c&m &#ff6c66&m&m &#ff8078&m &#ff948a&m &#ffa69d&m &#ffb8b0&m &#ffcac3&m &#ffdcd7&m &#ffedeb&m &f&m &f",
            "&f/emf admin competition start <duration> <type> - Starts a competition of a specified duration",
            "&f/emf admin competition end - Ends the current competition (if there is one)"
    ), PrefixType.ADMIN, false, true),

    INVALID_COMPETITION_TYPE("admin.competition-type-invalid", "&rThat isn't a type of competition type, available types: MOST_FISH, LARGEST_FISH, SPECIFIC_FISH", PrefixType.ADMIN, false, false),

    LEADERBOARD_LARGEST_FISH("leaderboard-largest", "&r#{position} | {pos_colour}{player} &r({rarity_colour}&l{rarity} {rarity_colour}{fish}&r, {length}cm&r)", PrefixType.DEFAULT, false, true),
    LEADERBOARD_LARGEST_TOTAL("leaderboard-largest-total", "&r#{position} | {pos_colour}{player} &r({pos_colour}{amount}cm&r)", PrefixType.DEFAULT, false, true),
    LEADERBOARD_MOST_FISH("leaderboard-most-fish", "&r#{position} | {pos_colour}{player} &r({pos_colour}{amount} &rfish)", PrefixType.DEFAULT, false, true),
    LEADERBOARD_TOTAL_PLAYERS("total-players", "&rThere are a total of {amount} player(s) in the leaderboard.", PrefixType.DEFAULT, true, true),

    NEW_FIRST_PLACE_NOTIFICATION("new-first", "&r{player} is now #1", PrefixType.DEFAULT, true, true),

    NO_BAITS("admin.no-baits-on-rod", "&rThe fishing rod does not have any baits applied.", PrefixType.ERROR, true, true),
    NO_COMPETITION_RUNNING("no-competition-running", "&rNo competition running right now.", PrefixType.ERROR, false, false),
    NO_FISH_CAUGHT("no-record", "&rYou didn't catch any fish.", PrefixType.DEFAULT, true, true),
    NO_PERMISSION_FISHING("no-permission-fishing", "&cYou don't have permission to fish using this rod, you will catch vanilla fish.", PrefixType.DEFAULT, false, true),
    NO_PERMISSION("no-permission", "&cYou don't have permission to run that command.", PrefixType.ERROR, false, true),
    NO_WINNERS("no-winners", "&rThere were no fishing records.", PrefixType.DEFAULT, true, true),
    NOT_ENOUGH_PLAYERS("not-enough-players", "&rThere's not enough players online to start the scheduled fishing competition.", PrefixType.ERROR, true, false),

    PLACEHOLDER_FISH_FORMAT("emf-competition-fish-format", "{rarity_colour}{length}cm &l{rarity} {fish}", PrefixType.NONE, true, false),
    PLACEHOLDER_FISH_LENGTHLESS_FORMAT("emf-lengthless-fish-format", "{rarity_colour}&l{rarity} {fish}", PrefixType.NONE, true, false),
    PLACEHOLDER_FISH_MOST_FORMAT("emf-most-fish-format", "{amount} fish", PrefixType.NONE, true, false),
    PLACEHOLDER_NO_COMPETITION_RUNNING("no-competition-running", "No competition running right now.", PrefixType.NONE, true, false),
    PLACEHOLDER_NO_PLAYER_IN_PLACE("no-player-in-place", "Start fishing to take this place", PrefixType.NONE, true, false),
    PLACEHOLDER_SIZE_DURING_MOST_FISH("emf-size-during-most-fish", "N/A", PrefixType.NONE, true, false),
    PLACEHOLDER_TIME_REMAINING("emf-time-remaining", "Time left until next competition: {days}d, {hours}h, {minutes}m.", PrefixType.NONE, true, false),
    PLACEHOLDER_TIME_REMAINING_DURING_COMP("emf-time-remaining-during-comp", "There is a competition running right now.", PrefixType.NONE, true, false),

    RELOAD_SUCCESS("admin.reload", "&rSuccessfully reloaded the plugin.", PrefixType.ADMIN, false, false),

    TIME_ALERT("time-alert", "&rThere is {time_formatted} left on the competition for {type}", PrefixType.DEFAULT, false, true),

    TOGGLE_ON("toggle-on", "&rYou will now catch custom fish.", PrefixType.DEFAULT, false, true),
    TOGGLE_OFF("toggle-off", "&rYou will no longer catch custom fish.", PrefixType.DEFAULT, false, true),

    WORTH_GUI_NAME("worth-gui-name", "&1&lSell Fish", PrefixType.NONE, false, false),
    WORTH_GUI_CONFIRM_ALL_BUTTON_NAME("confirm-sell-all-gui-name", "&6&lCONFIRM", PrefixType.NONE, false, false),
    WORTH_GUI_CONFIRM_BUTTON_NAME("confirm-gui-name", "&6&lCONFIRM", PrefixType.NONE, false, false),
    WORTH_GUI_NO_VAL_BUTTON_NAME("error-gui-name", "&c&lCan't Sell", PrefixType.NONE, false, false),
    WORTH_GUI_NO_VAL_BUTTON_LORE("error-gui-lore", Arrays.asList(
            "&8Fish Shop",
            "",
            "&7Total Value » &c$0",
            "",
            "&7Sell your fish here to make",
            "&7some extra money.",
            "",
            "&c» (Left-click) sell the fish.",
            "&7» (Right-click) cancel."
    ), PrefixType.NONE, false, false),
    WORTH_GUI_NO_VAL_ALL_BUTTON_NAME("error-sell-all-gui-name", "&c&lCan't Sell", PrefixType.NONE, false, false),
    WORTH_GUI_SELL_ALL_BUTTON_NAME("sell-all-name", "&6&lSELL ALL", PrefixType.NONE, false, false),
    WORTH_GUI_SELL_ALL_BUTTON_LORE("sell-all-lore", Arrays.asList(
            "&e&lValue: &e${sell-price}", "&7LEFT CLICK to sell all fish in your inventory."
    ), PrefixType.NONE, false, false),
    WORTH_GUI_SELL_BUTTON_NAME("sell-gui-name", "&6&lSELL", PrefixType.NONE, false, false),
    WORTH_GUI_SELL_BUTTON_LORE("error-sell-all-gui-lore", Arrays.asList(
            "&8Inventory",
            "",
            "&7Total Value » &c$0",
            "",
            "&7Click this button to sell",
            "&7the fish in your inventory to",
            "&7make some extra money.",
            "",
            "&c» (Left-click) sell the fish."
    ), PrefixType.NONE, false, false),
    WORTH_GUI_SELL_LORE("sell-gui-lore", Arrays.asList(
            "&8Fish Shop",
            "",
            "&7Total Value » &e${sell-price}",
            "",
            "&7Sell your fish here to make",
            "&7some extra money.",
            "",
            "&e» (Left-click) sell the fish.",
            "&7» (Right-click) cancel."
    ), PrefixType.NONE, false, false);

    private final String id;
    private final boolean canSilent, canHidePrefix;
    private final PrefixType prefixType;
    private String normal;
    private List<String> normalList;

    /**
     * This is the config enum for a value in the messages.yml file. It does not store the actual data but references
     * where to look in the file for the data. This must be passed through a Message object before it can be sent to
     * players. In there, it is possible to add variable options, and it will be colour formatted too.
     *
     * @param id            The id in messages.yml for the ConfigMessage.
     * @param normal        The default value in the base messages.yml.
     * @param prefixType    The type of prefix that should be used in this instance.
     * @param canSilent     If the message can be sent silently (not sent).
     * @param canHidePrefix If the message can have the [noPrefix] applied to remove the prefix.
     */
    ConfigMessage(String id, String normal, PrefixType prefixType, boolean canSilent, boolean canHidePrefix) {
        this.id = id;
        this.normal = normal;
        this.canSilent = canSilent;
        this.canHidePrefix = canHidePrefix;
        this.prefixType = prefixType;
    }

    /**
     * This is the config enum for a list value in the messages.yml file. It does not store the actual data but references
     * where to look in the file for the data. This must be passed through a Message object before it can be sent to
     * players. In there, it is possible to add variable options, and it will be colour formatted too. It also must be
     * a list within the file.
     *
     * @param id            The id in messages.yml for the ConfigMessage.
     * @param normalList    The default value for the list in the base messages.yml.
     * @param prefixType    The type of prefix that should be used in this instance.
     * @param canSilent     If the message can be sent silently (not sent).
     * @param canHidePrefix If the message can have the [noPrefix] applied to remove the prefix.
     */
    ConfigMessage(String id, List<String> normalList, PrefixType prefixType, boolean canSilent, boolean canHidePrefix) {
        this.id = id;
        this.normalList = normalList;
        this.canSilent = canSilent;
        this.canHidePrefix = canHidePrefix;
        this.prefixType = prefixType;
    }


    public String getId() {
        return this.id;
    }

    public String getNormal() {
        return this.normal;
    }

    public List<String> getNormalList() {
        return this.normalList;
    }

    public boolean isCanSilent() {
        return this.canSilent;
    }

    public boolean isCanHidePrefix() {
        return this.canHidePrefix;
    }

    public boolean isListForm() {
        return this.normal == null;
    }

    public PrefixType getPrefixType() {
        return prefixType;
    }
}

