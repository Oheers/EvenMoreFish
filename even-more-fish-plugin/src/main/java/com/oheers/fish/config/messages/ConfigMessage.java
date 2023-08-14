package com.oheers.fish.config.messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ConfigMessage {

    ADMIN_CANT_BE_CONSOLE("&rCommand cannot be run from console.", PrefixType.ERROR, false, true, "admin.cannot-run-on-console"),
    ADMIN_GIVE_PLAYER_BAIT("&rYou have given {player} a {bait}.", PrefixType.ADMIN, true, true, "admin.given-player-bait"),
    ADMIN_GIVE_PLAYER_FISH("&rYou have given {player} a {fish}.", PrefixType.ADMIN, true, true, "admin.given-player-fish"),
    ADMIN_OPEN_FISH_SHOP("&rOpened a shop inventory for {player}.", PrefixType.ADMIN, true, true, "admin.open-fish-shop"),
    ADMIN_NBT_ROD_GIVEN("&rYou have given {player} a NBT rod, make sure \"require-nbt-rod\" is set to &atrue &rfor this to be different from any other fishing rod.", PrefixType.ADMIN, true, true, "admin.nbt-rod-given"),
    ADMIN_NBT_NOT_REQUIRED("&rChange \"require-nbt-rod\" to true in order to use this feature.", PrefixType.ERROR, false, true, "admin.nbt-not-required"),
    ADMIN_NO_BAIT_SPECIFIED("&rYou must specify a bait name.", PrefixType.ERROR, false, true, "admin.no-bait-specified"),
    ADMIN_NOT_HOLDING_ROD("&rYou need to be holding a fishing rod to run that command.", PrefixType.ERROR, false, false, "admin.must-be-holding-rod"),
    ADMIN_NUMBER_FORMAT_ERROR("&r{amount} is not a valid number.", PrefixType.ERROR, false, true, "admin.number-format-error"),
    ADMIN_NUMBER_RANGE_ERROR("&r{amount} is not a number between 1-64.", PrefixType.ERROR, false, true, "admin.number-range-error"),
    ADMIN_UNKNOWN_PLAYER("&r{player} could not be found.", PrefixType.ERROR, false, true, "admin.player-not-found"),
    ADMIN_UPDATE_AVAILABLE("&rThere is an update available: " + "https://www.spigotmc.org/resources/evenmorefish.91310/updates", PrefixType.ADMIN, false, false, "admin.update-available"),

    BAITS_CLEARED("&rYou have removed all {amount} baits from your fishing rod.", PrefixType.ADMIN, true, false, "admin.all-baits-cleared"),
    BAIT_CAUGHT("&r&l{player} &rhas caught a {bait_theme}&l{bait} &rbait!", PrefixType.NONE, true, false, "bait-catch"),
    BAIT_USED("&rYou have used one of your rod's {bait_theme}&l{bait} &rbait.", PrefixType.DEFAULT, true, false, "bait-use"),
    BAIT_WRONG_GAMEMODE("&rYou must be in &nsurvival&r to apply baits to fishing rods.", PrefixType.ERROR, false, false, "bait-survival-limited"),
    BAITS_MAXED("&rYou have reached the maximum number of types of baits for this fishing rod.", PrefixType.DEFAULT, false, true, "max-baits-reached"),
    BAITS_MAXED_ON_ROD("&rYou have reached the maximum number of {bait_theme}{bait} &rbait that can be applied to one rod.", PrefixType.ERROR, false, false, "max-baits-reached"),

    BAR_SECOND("s", PrefixType.NONE, true, false, "bossbar.second"),
    BAR_MINUTE("m", PrefixType.NONE, true, false, "bossbar.minute"),
    BAR_HOUR("h", PrefixType.NONE, true, false, "bossbar.hour"),
    BAR_REMAINING("left", PrefixType.NONE, true, false, "bossbar.remaining"),

    COMPETITION_ALREADY_RUNNING("&rThere's already a competition running.", PrefixType.ADMIN, false, true, "admin.competition-already-running"),

    COMPETITION_END("&rThe fishing contest has ended.", PrefixType.DEFAULT, false, false, "contest-end"),
    COMPETITION_JOIN("&rA fishing contest for {type} is going on.", PrefixType.DEFAULT, true, false, "contest-join"),
    COMPETITION_START("&rA fishing contest for {type} has started.", PrefixType.DEFAULT, false, false, "contest-start"),

    COMPETITION_TYPE_LARGEST("the largest fish", PrefixType.NONE, true, false, "competition-types.largest"),
    COMPETITION_TYPE_LARGEST_TOTAL("the largest total fish length", PrefixType.NONE, true, false, "competition-types.largest-total"),
    COMPETITION_TYPE_MOST("the most fish", PrefixType.NONE, true, false, "competition-types.most"),
    COMPETITION_TYPE_SPECIFIC("{amount} {rarity_colour}&l{rarity} {rarity_colour}{fish}&r", PrefixType.NONE, true, false, "competition-types.specific"),
    COMPETITION_TYPE_SPECIFIC_RARITY("{amount} {rarity_colour}&l{rarity}&r fish", PrefixType.NONE, true, false, "competition-types.specific-rarity"),

    COMPETITION_SINGLE_WINNER("&r{player} has won the competition for {type}. Congratulations!", PrefixType.DEFAULT, true, true, "single-winner"),

    ECONOMY_DISABLED("&rEvenMoreFish's economy features are disabled.", PrefixType.ERROR, false, false, "admin.economy-disabled"),

    FISH_CANT_BE_PLACED("&rYou cannot place this fish.", PrefixType.ERROR, true, true, "place-fish-blocked"),
    FISH_CAUGHT("&r&l{player} &rhas fished a {rarity_colour}{length}cm &l{rarity} {rarity_colour}{fish}!", PrefixType.NONE, true, false, "fish-caught"),
    FISH_LENGTHLESS_CAUGHT("&r&l{player} &rhas fished a {rarity_colour}&l{rarity} {rarity_colour}{fish}!", PrefixType.NONE, true, false, "lengthless-fish-caught"),
    FISH_LORE(Arrays.asList(
            "{fisherman_lore}",
            "{length_lore}",
            "",
            "{fish_lore}",
            "{rarity_colour}&l{rarity}"
    ), PrefixType.NONE, false, false, "fish-lore"),
    FISHERMAN_LORE(Collections.singletonList(
            "&fCaught by {player}"
    ), PrefixType.NONE, false, false, "fisherman-lore"),
    LENGTH_LORE(Collections.singletonList(
            "&fMeasures {length}cm"
    ), PrefixType.NONE, false, false, "length-lore"),
    FISH_SALE("&rYou've sold &a{amount} &ffish for &a{sell-price}&f.", PrefixType.DEFAULT, true, true, "fish-sale"),

    HELP_GENERAL(Arrays.asList(
            "&f&m &#f1ffed&m &#e2ffdb&m &#d3ffc9&m &#c3ffb7&m &#b2ffa5&m &#9fff92&m &#8bff7f&m &#73ff6b&m &a&m &f &a&lEvenMoreFish &a&m &#73ff6b&m&m &#8bff7f&m &#9fff92&m &#b2ffa5&m &#c3ffb7&m &#d3ffc9&m &#e2ffdb&m &#f1ffed&m &f&m &f",
            "&f/emf top - Shows an ongoing competition's leaderboard.",
            "&f/emf help - Shows you this page.",
            "&f/emf shop - Opens a shop to sell your fish.",
            "&f/emf toggle - Toggles whether or not you receive custom fish.",
            "&f/emf admin - Admin command help page."
    ), PrefixType.DEFAULT, false, true, "help-general"),
    HELP_ADMIN(Arrays.asList(
            "&f&m &#ffedeb&m &#ffdcd7&m &#ffcac3&m &#ffb8b0&m &#ffa69d&m &#ff948a&m &#ff8078&m &#ff6c66&m &c&m &f &c&lEvenMoreFish &c&m &#ff6c66&m&m &#ff8078&m &#ff948a&m &#ffa69d&m &#ffb8b0&m &#ffcac3&m &#ffdcd7&m &#ffedeb&m &f&m &f",
            "&f/emf admin competition <start/end> <duration> <type> - Starts or stops a competition",
            "&f/emf admin clearbait - Removes all applied baits from a fishing rod.",
            "&f/emf admin reload - Reloads the plugin's config files",
            "&f/emf admin version - Displays plugin information."
    ), PrefixType.ADMIN, false, true, "help-admin"),
    HELP_COMPETITION(Arrays.asList(
            "&f&m &#ffedeb&m &#ffdcd7&m &#ffcac3&m &#ffb8b0&m &#ffa69d&m &#ff948a&m &#ff8078&m &#ff6c66&m &c&m &f &c&lEvenMoreFish &c&m &#ff6c66&m&m &#ff8078&m &#ff948a&m &#ffa69d&m &#ffb8b0&m &#ffcac3&m &#ffdcd7&m &#ffedeb&m &f&m &f",
            "&f/emf admin competition start <duration> <type> - Starts a competition of a specified duration",
            "&f/emf admin competition end - Ends the current competition (if there is one)"
    ), PrefixType.ADMIN, false, true, "help-competition"),

    INVALID_COMPETITION_TYPE("&rThat isn't a type of competition type, available types: MOST_FISH, LARGEST_FISH, SPECIFIC_FISH", PrefixType.ADMIN, false, false, "admin.competition-type-invalid"),

    LEADERBOARD_LARGEST_FISH("&r#{position} | {pos_colour}{player} &r({rarity_colour}&l{rarity} {rarity_colour}{fish}&r, {length}cm&r)", PrefixType.DEFAULT, false, true, "leaderboard-largest-fish"),
    LEADERBOARD_LARGEST_TOTAL("&r#{position} | {pos_colour}{player} &r({pos_colour}{amount}cm&r)", PrefixType.DEFAULT, false, true, "leaderboard-largest-total"),
    LEADERBOARD_MOST_FISH("&r#{position} | {pos_colour}{player} &r({pos_colour}{amount} &rfish)", PrefixType.DEFAULT, false, true, "leaderboard-most-fish"),
    LEADERBOARD_TOTAL_PLAYERS("&rThere are a total of {amount} player(s) in the leaderboard.", PrefixType.DEFAULT, true, true, "total-players"),

    NEW_FIRST_PLACE_NOTIFICATION("&r{player} is now #1", PrefixType.DEFAULT, true, true, "new-first"),

    NO_BAITS("&rThe fishing rod does not have any baits applied.", PrefixType.ERROR, true, true, "admin.no-baits-on-rod"),
    NO_COMPETITION_RUNNING("&rNo competition running right now.", PrefixType.ERROR, false, false, "no-competition-running"),
    NO_FISH_CAUGHT("&rYou didn't catch any fish.", PrefixType.DEFAULT, true, true, "no-record"),
    NO_PERMISSION_FISHING("&cYou don't have permission to fish using this rod, you will catch vanilla fish.", PrefixType.DEFAULT, true, true, "no-permission-fishing"),
    NO_PERMISSION("&cYou don't have permission to run that command.", PrefixType.ERROR, false, true, "no-permission"),
    NO_WINNERS("&rThere were no fishing records.", PrefixType.DEFAULT, true, true, "no-winners"),
    NOT_ENOUGH_PLAYERS("&rThere's not enough players online to start the scheduled fishing competition.", PrefixType.ERROR, true, false, "not-enough-players"),

    PLACEHOLDER_FISH_FORMAT("{rarity_colour}{length}cm &l{rarity} {fish}", PrefixType.NONE, true, false, "emf-competition-fish-format"),
    PLACEHOLDER_FISH_LENGTHLESS_FORMAT("{rarity_colour}&l{rarity} {fish}", PrefixType.NONE, true, false, "emf-lengthless-fish-format"),
    PLACEHOLDER_FISH_MOST_FORMAT("{amount} fish", PrefixType.NONE, true, false, "emf-most-fish-format"),
    PLACEHOLDER_NO_COMPETITION_RUNNING("No competition running right now.", PrefixType.NONE, true, false, "no-competition-running"),
    PLACEHOLDER_NO_COMPETITION_RUNNING_FISH("No competition running right now.", PrefixType.NONE, true, false, "no-competition-running-fish", "no-competition-running"),
    PLACEHOLDER_NO_COMPETITION_RUNNING_SIZE("No competition running right now.", PrefixType.NONE, true, false, "no-competition-running-size", "no-competition-running"),

    PLACEHOLDER_NO_PLAYER_IN_PLACE("Start fishing to take this place", PrefixType.NONE, true, false, "no-player-in-place"),
    PLACEHOLDER_NO_FISH_IN_PLACE("Start fishing to take this place", PrefixType.NONE, true, false, "no-fish-in-place", "no-player-in-place"),
    PLACEHOLDER_NO_SIZE_IN_PLACE("Start fishing to take this place", PrefixType.NONE, true, false, "no-size-in-place", "no-player-in-place"),
    PLACEHOLDER_SIZE_DURING_MOST_FISH("N/A", PrefixType.NONE, true, false, "emf-size-during-most-fish"),
    PLACEHOLDER_TIME_REMAINING("Time left until next competition: {days}d, {hours}h, {minutes}m.", PrefixType.NONE, true, false, "emf-time-remaining"),
    PLACEHOLDER_TIME_REMAINING_DURING_COMP("There is a competition running right now.", PrefixType.NONE, true, false, "emf-time-remaining-during-comp"),

    RELOAD_SUCCESS("&rSuccessfully reloaded the plugin.", PrefixType.ADMIN, false, false, "admin.reload"),
    SELL_PRICE_FORMAT("#,##0.0", PrefixType.NONE, false, false, "sell-price-format"),
    TIME_ALERT("&rThere is {time_formatted} left on the competition for {type}", PrefixType.DEFAULT, false, true, "time-alert"),

    TOGGLE_ON("&rYou will now catch custom fish.", PrefixType.DEFAULT, false, true, "toggle-on"),
    TOGGLE_OFF("&rYou will no longer catch custom fish.", PrefixType.DEFAULT, false, true, "toggle-off"),

    WORTH_GUI_NAME("&1&lSell Fish", PrefixType.NONE, false, false, "worth-gui-name"),
    WORTH_GUI_CONFIRM_ALL_BUTTON_NAME("&6&lCONFIRM", PrefixType.NONE, false, false, "confirm-sell-all-gui-name"),
    WORTH_GUI_CONFIRM_BUTTON_NAME("&6&lCONFIRM", PrefixType.NONE, false, false, "confirm-gui-name"),
    WORTH_GUI_NO_VAL_BUTTON_NAME("&c&lCan't Sell", PrefixType.NONE, false, false, "error-gui-name"),
    WORTH_GUI_NO_VAL_BUTTON_LORE(Arrays.asList(
            "&8Fish Shop",
            "",
            "&7Total Value » &c{sell-price}",
            "",
            "&7Sell your fish here to make",
            "&7some extra money.",
            "",
            "&c» (Left-click) sell the fish.",
            "&7» (Right-click) cancel."
    ), PrefixType.NONE, false, false, "error-gui-lore"),
    WORTH_GUI_NO_VAL_ALL_BUTTON_NAME("&c&lCan't Sell", PrefixType.NONE, false, false, "error-sell-all-gui-name"),
    WORTH_GUI_SELL_ALL_BUTTON_NAME("&6&lSELL ALL", PrefixType.NONE, false, false, "sell-all-name"),
    WORTH_GUI_SELL_ALL_BUTTON_LORE(Arrays.asList(
            "&e&lValue: &e${sell-price}", "&7LEFT CLICK to sell all fish in your inventory."
    ), PrefixType.NONE, false, false, "sell-all-lore"),
    WORTH_GUI_SELL_BUTTON_NAME("&6&lSELL", PrefixType.NONE, false, false, "sell-gui-name"),
    WORTH_GUI_SELL_BUTTON_LORE(Arrays.asList(
            "&8Inventory",
            "",
            "&7Total Value » &c{sell-price}",
            "",
            "&7Click this button to sell",
            "&7the fish in your inventory to",
            "&7make some extra money.",
            "",
            "&c» (Left-click) sell the fish."
    ), PrefixType.NONE, false, false, "error-sell-all-gui-lore"),
    WORTH_GUI_SELL_LORE(Arrays.asList(
            "&8Fish Shop",
            "",
            "&7Total Value » &e{sell-price}",
            "",
            "&7Sell your fish here to make",
            "&7some extra money.",
            "",
            "&e» (Left-click) sell the fish.",
            "&7» (Right-click) cancel."
    ), PrefixType.NONE, false, false, "sell-gui-lore");

    private final String[] id;
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
    ConfigMessage(String normal, PrefixType prefixType, boolean canSilent, boolean canHidePrefix, String... id) {
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
    ConfigMessage(List<String> normalList, PrefixType prefixType, boolean canSilent, boolean canHidePrefix, String... id) {
        this.id = id;
        this.normalList = normalList;
        this.canSilent = canSilent;
        this.canHidePrefix = canHidePrefix;
        this.prefixType = prefixType;
    }


    public String[] getIds() {
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

