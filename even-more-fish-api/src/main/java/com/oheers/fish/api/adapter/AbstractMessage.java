package com.oheers.fish.api.adapter;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractMessage {

    private Boolean papiEnabled = null;
    private final PlatformAdapter platformAdapter;
    private final Map<String, String> liveVariables = new LinkedHashMap<>();
    private String message;
    private boolean canSilent = false;
    private OfflinePlayer relevantPlayer;

    protected AbstractMessage(@NotNull final String message, @NotNull PlatformAdapter platformAdapter) {
        this.message = formatColours(message);
        this.platformAdapter = platformAdapter;
    }

    protected AbstractMessage(@NotNull final List<String> messageList, @NotNull PlatformAdapter platformAdapter) {
        String reset = formatColours("&r");
        this.message = String.join( reset + "\n", messageList.stream().map(this::formatColours).toList());
        this.platformAdapter = platformAdapter;
    }

    public void setMessage(@NotNull String message) {
        this.message = formatColours(message);
    }

    public void setMessage(@NotNull AbstractMessage message) {
        this.message = message.getRawMessage();
    }

    protected boolean isPAPIEnabled() {
        if (papiEnabled == null) {
            papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        }
        return papiEnabled;
    }

    /**
     * Converts colors to the format this impl requires.
     * @param message The message to process
     * @return The processed message.
     */
    public abstract String formatColours(@NotNull String message);

    /**
     * Formats all variables in {@link #liveVariables}
     */
    protected void formatVariables() {
        for (Map.Entry<String, String> entry : liveVariables.entrySet()) {
            String variable = entry.getKey();
            String replacement = formatColours(entry.getValue());
            this.message = this.message.replace(variable, replacement);
        }
    }

    /**
     * Sends this message to the entire server.
     */
    public void broadcast() {
        send(Bukkit.getConsoleSender());
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    /**
     * Sends this message to the provided target.
     * @param target The target of this message.
     */
    public abstract void send(@NotNull CommandSender target);

    /**
     * Sends this message to the provided list of targets.
     * @param targets The targets of this message.
     */
    public void send(@NotNull List<CommandSender> targets) {
        targets.forEach(this::send);
    }

    /**
     * Sends this message to the entire server as an action bar.
     */
    public void broadcastActionBar() {
        Bukkit.getOnlinePlayers().forEach(this::sendActionBar);
    }

    /**
     * Sends this message to the provided target as an action bar.
     * @param target The target of this message.
     */
    public abstract void sendActionBar(@NotNull CommandSender target);

    /**
     * Sends this message to the provided list of targets as an action bar.
     * @param targets The targets of this message.
     */
    public void sendActionBar(@NotNull List<CommandSender> targets) {
        targets.forEach(this::sendActionBar);
    }

    /**
     * @return The stored String in its raw form, with no colors or variables applied.
     */
    public @NotNull String getRawMessage() {
        return this.message;
    }

    /**
     * @return The stored String in its raw list form, with no colors or variables applied.
     */
    public @NotNull List<String> getRawListMessage() {
        return Arrays.asList(this.message.split("\n"));
    }

    /**
     * @return The formatted message as a legacy string, both colors and variables will be formatted.
     */
    public abstract String getLegacyMessage();

    /**
     * @return The formatted message as a legacy string list, both colors and variables will be formatted.
     */
    public @NotNull List<String> getLegacyListMessage() {
        return Arrays.asList(getLegacyMessage().split("\n"));
    }

    /**
     * Formats PlaceholderAPI placeholders.
     * <p>
     * This is abstract because the MiniMessage impl will need to handle this manually.
     */
    public abstract void formatPlaceholderAPI();

    /**
     * Adds the provided string to the end of this message.
     * @param message The string to append
     */
    public void appendString(@NotNull String message) {
        this.message = this.message + formatColours(message);
    }

    /**
     * Adds the provided message to the end of this message.
     * @param message The message to append
     */
    public void appendMessage(@NotNull AbstractMessage message) {
        appendString(message.getRawMessage());
    }

    /**
     * Adds the provided strings to the end of this message.
     * @param messages The strings to append
     */
    public void appendStringList(@NotNull List<String> messages) {
        this.message = this.message + String.join("\n", messages.stream().map(this::formatColours).toList());
    }

    /**
     * Adds the provided messages to the end of this message.
     * @param messages The messages to append
     */
    public void appendMessageList(@NotNull List<AbstractMessage> messages) {
        StringBuilder newMessage = new StringBuilder(this.message);
        for (AbstractMessage message : messages) {
            newMessage.append(message.getRawMessage());
        }
        this.message = newMessage.toString();
    }

    /**
     * Adds the provided string to the start of this message.
     * @param message The string to prepend
     */
    public void prependString(@NotNull String message) {
        this.message = formatColours(message) + this.message;
    }

    /**
     * Adds the provided message to the start of this message.
     * @param message The message to prepend
     */
    public void prependMessage(@NotNull AbstractMessage message) {
        prependString(message.getRawMessage());
    }

    /**
     * Adds the provided strings to the start of this message.
     * @param messages The strings to prepend
     */
    public void prependStringList(@NotNull List<String> messages) {
        this.message = String.join("\n", messages.stream().map(this::formatColours).toList()) + this.message;
    }

    /**
     * Adds the provided messages to the start of this message.
     * @param messages The messages to prepend
     */
    public void prependMessageList(@NotNull List<AbstractMessage> messages) {
        StringBuilder newMessage = new StringBuilder();
        for (AbstractMessage message : messages) {
            newMessage.append(message.getRawMessage());
        }
        this.message = newMessage.toString() + this.message;
    }

    /**
     * @param canSilent Should this message support -s?
     */
    public void setCanSilent(boolean canSilent) {
        this.canSilent = canSilent;
    }

    /**
     * @return Does this message support -s?
     */
    public boolean isCanSilent() {
        return this.canSilent;
    }

    /**
     * @return Should this message be silent?
     */
    public boolean silentCheck() {
        return canSilent && this.message.endsWith(" -s");
    }

    /**
     * Sets the relevant player for this message.
     * @param player The relevant player.
     */
    public void setRelevantPlayer(@Nullable OfflinePlayer player) {
        this.relevantPlayer = player;
    }

    /**
     * @return This message's relevant player, or null if not available.
     */
    public @Nullable OfflinePlayer getRelevantPlayer() {
        return this.relevantPlayer;
    }

    /**
     * @return The PlatformAdapter that created this message instance.
     */
    public @NotNull PlatformAdapter getPlatformAdapter() {
        return this.platformAdapter;
    }

    /**
     * Adds a variable to be formatted when {@link #formatVariables()} is called.
     * @param variable The variable.
     * @param replacement The replacement for the variable.
     */
    public void setVariable(@NotNull final String variable, @NotNull final String replacement) {
        this.liveVariables.put(variable, replacement);
    }

    /**
     * Adds a map of variables to be formatted when {@link #formatVariables()} is called.
     * @param variableMap The map of variables and their replacements.
     */
    public void setVariables(@Nullable Map<String, String> variableMap) {
        if (variableMap == null) { return; }
        this.liveVariables.putAll(variableMap);
    }

    // Variable Shortcuts

    /**
     * The colour used by the fish's rarity to apply a clean format for the fish, to replace the {rarity_colour} variable.
     *
     * @param colour The &_ code for the colour.
     */
    public void setRarityColour(@NotNull final String colour) {
        setVariable("{rarity_colour}", colour);
    }

    /**
     * The player's name to replace the {player} variable. Also sets the relevantPlayer variable to this player.
     *
     * @param player The player.
     */
    public void setPlayer(@NotNull final OfflinePlayer player) {
        this.relevantPlayer = player;
        setVariable("{player}", Objects.requireNonNullElse(player.getName(), "N/A"));
    }

    /**
     * The fish's length to replace the {length} variable.
     *
     * @param length The length of the fish.
     */
    public void setLength(@NotNull final String length) {
        setVariable("{length}", length);
    }

    /**
     * The rarity of the fish to replace the {rarity} variable.
     *
     * @param rarity The fish's rarity.
     */
    public void setRarity(@NotNull final String rarity) {
        setVariable("{rarity}", rarity);
    }

    /**
     * Sets the fish name to replace the {fish} variable.
     *
     * @param fish The fish's name.
     */
    public void setFishCaught(@NotNull final String fish) {
        setVariable("{fish}", fish);
    }

    /**
     * The price after a fish has been sold in /emf shop to replace the {sell-price} variable.
     *
     * @param sellPrice The sell price of the fish.
     */
    public void setSellPrice(@NotNull final String sellPrice) {
        setVariable("{sell-price}", sellPrice);
    }

    /**
     * The amount of whatever, used multiple times throughout the plugin to replace the {amount} variable.
     *
     * @param amount The amount of x.
     */
    public void setAmount(@NotNull final String amount) {
        setVariable("{amount}", amount);
    }

    /**
     * Sets the position for the {position} variable in the /emf top leaderboard.
     *
     * @param position The position.
     */
    public void setPosition(@NotNull final String position) {
        setVariable("{position}", position);
    }

    /**
     * Sets the colour of the position for the {pos_colour} variable in the /emf top leaderboard.
     *
     * @param positionColour The position.
     */
    public void setPositionColour(@NotNull final String positionColour) {
        setVariable("{pos_colour}", positionColour);
    }

    /**
     * Sets the formatted (Nh, Nm, Ns) time to replace the {time_formatted} variable.
     *
     * @param timeFormatted The formatted time.
     */
    public void setTimeFormatted(@NotNull final String timeFormatted) {
        setVariable("{time_formatted}", timeFormatted);
    }

    /**
     * Sets the raw time (Nh:Nm:Ns) time to replace the {time_raw} variable.
     *
     * @param timeRaw The raw time.
     */
    public void setTimeRaw(@NotNull final String timeRaw) {
        setVariable("{time_raw}", timeRaw);
    }

    /**
     * Sets the bait in the message to replace the {bait} variable.
     *
     * @param bait The name of the bait.
     */
    public void setBait(@NotNull final String bait) {
        setVariable("{bait}", bait);
    }

    /**
     * Defines the theme of the bait to be used throughout the message to replace the {bait_theme} variable.
     *
     * @param baitTheme The bait colour theme.
     */
    public void setBaitTheme(@NotNull final String baitTheme) {
        setVariable("{bait_theme}", baitTheme);
    }


    /**
     * Defines how many days should replace the {days} variable.
     *
     * @param days The number of days.
     */
    public void setDays(@NotNull final String days) {
        setVariable("{days}", days);
    }

    /**
     * Defines how many hours should replace the {hours} variable.
     *
     * @param hours The number of hours.
     */
    public void setHours(@NotNull final String hours) {
        setVariable("{hours}", hours);
    }

    /**
     * Defines how many minutes should replace the {minutes} variable.
     *
     * @param minutes The number of minutes.
     */
    public void setMinutes(@NotNull final String minutes) {
        setVariable("{minutes}", minutes);
    }

    /**
     * Defines the result for the toggle MSG to replace the {toggle_msg} variable.
     *
     * @param toggleMSG The applicable toggle msg.
     */
    public void setToggleMSG(@NotNull final String toggleMSG) {
        setVariable("{toggle_msg}", toggleMSG);
    }

    /**
     * Defines the result for the toggle material to replace the {toggle_icon} variable.
     *
     * @param toggleIcon The applicable toggle material.
     */
    public void setToggleIcon(@NotNull final String toggleIcon) {
        setVariable("{toggle_icon}", toggleIcon);
    }

    /**
     * Defines which day should replace the {day} variable.
     *
     * @param day The day number.
     */
    public void setDay(@NotNull final String day) {
        setVariable("{day}", day);
    }

    /**
     * Defines the name of the fish to be used, alternate to {fish}.
     *
     * @param name The name of the fish or user
     */
    public void setName(@NotNull final String name) {
        setVariable("{name}", name);
    }

    /**
     * Defines the number of fish caught in the user's fish reports.
     *
     * @param numCaught The number of fish caught.
     */
    public void setNumCaught(@NotNull final String numCaught) {
        setVariable("{num_caught}", numCaught);
    }

    /**
     * Defines the largest fish caught by the user in their fish reports.
     *
     * @param largestSize The largest size of the fish.
     */
    public void setLargestSize(@NotNull final String largestSize) {
        setVariable("{largest_size}", largestSize);
    }

    /**
     * The first fish to be caught by the user in their fish reports.
     *
     * @param firstCaught The first fish caught.
     */
    public void setFirstCaught(@NotNull final String firstCaught) {
        setVariable("{first_caught}", firstCaught);
    }

    /**
     * The time remaining for the fish to be unlocked.
     *
     * @param timeRemaining The time remaining.
     */
    public void setTimeRemaining(@NotNull final String timeRemaining) {
        setVariable("{time_remaining}", timeRemaining);
    }

    /**
     * Sets the competition type, checking against the values for each type stored in messages.yml to replace the {type}
     * variable.
     *
     * @param type The competition type.
     */
    public void setCompetitionType(@NotNull final String typeString) {
        setVariable("{type}", typeString);
    }

    /**
     * The amount of baits currently applied to the item.
     *
     * @param currentBaits The amount of baits.
     */
    public void setCurrentBaits(String currentBaits) {
        setVariable("{current_baits}", currentBaits);
    }

    /**
     * The max amount of baits that can be applied to the item.
     *
     * @param maxBaits The max amount of baits.
     */
    public void setMaxBaits(String maxBaits) {
        setVariable("{max_baits}", maxBaits);
    }

}
