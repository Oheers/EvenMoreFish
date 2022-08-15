package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.CompetitionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");
    private static final char COLOR_CHAR = '\u00A7';
    private final Map<String, String> liveVariables = new LinkedHashMap<>();
    public String message;
    private boolean canSilent, canHidePrefix;

    /**
     * EMF system of sending messages. A raw message with variables is passed through the "message" parameter. Variables
     * can be set to the object which will be translated live when either of the broadcast() methods are used. PlaceholderAPI
     * will also be run through during this period too.
     *
     * @param message The raw message being translated. Hex and & codes are supported.
     */
    public Message(@NotNull final String message) {
        this.message = message;
        this.canSilent = false;
        this.canHidePrefix = false;
    }

    /**
     * EMF system of sending list messages. The ConfigMessage is converted to a string with line breaks and stored as a
     * message. If it is possible to start without a prefix, the [noPrefix] check will be run, and they will not be added.
     * Else, they will be added automatically.
     *
     * @param message The ConfigMessage object.
     */
    public Message(@NotNull final ConfigMessage message) {
        this.canSilent = message.isCanSilent();
        this.canHidePrefix = message.isCanHidePrefix();
        configMessageToString(message);
    }

    /**
     * When a ConfigMessage is passed through and set as the message for the object. If the config setting is a list then
     * each line is converted to a long list separated by multiple \n. Variables can be sent to the object which will be
     * translated live then either of the broadcast() methods are used. PlaceholderAPI will also be run through during this
     * period too.
     *
     * @param message The ConfigMessage to be converted.
     */
    private void configMessageToString(@NotNull final ConfigMessage message) {
        if (message.isListForm()) {
            this.message = "";
            List<String> list = getStringList(message.getId(), message.getNormalList());
            for (String line : list) {

                if (this.canHidePrefix && line.startsWith("[noPrefix]"))
                    this.message = this.message.concat(line.substring(10));
                else this.message = this.message.concat(message.getPrefixType().getPrefix() + line);

                if (!Objects.equals(line, list.get(list.size() - 1))) this.message = this.message.concat("\n");
            }
        } else {
            String line = getString(message.getId(), message.getNormal());

            if (this.canHidePrefix && line.startsWith("[noPrefix]")) this.message = line.substring(10);
            else this.message = message.getPrefixType().getPrefix() + line;
        }
    }

    /**
     * Converts &_ and &#______ formats to be converted into colour for the message. This should be run after the variables
     * have been formatted.
     */
    private void colourFormat() {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        this.message = ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    /**
     * Goes through all the set variables and replaces the message with their values - this should not be done where
     * each player needs sending a custom message since it'll only be run once. It should also be done before any colour
     * formatting in case they contain colour codes.
     */
    private void variableFormat() {
        for (String variable : liveVariables.keySet()) {
            this.message = this.message.replace(variable, liveVariables.get(variable));
        }
    }

    /**
     * Sends a global message to all online users, formatting the message for each user and applying placeholders where
     * necessary, where the placeholder acts on the user being sent the message.
     *
     * @param doColour    If the method should format for colours or not.
     * @param doVariables If variables should be formatted or not.
     */
    public void broadcast(final boolean doColour, final boolean doVariables) {
        if (doVariables) variableFormat();
        if (doColour) colourFormat();
        if (this.message.endsWith(" -s") && this.canSilent) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(this.message);
        }
    }

    /**
     * Sends a message to just one player, the message is formatted and the placeholder is directed to them.
     *
     * @param player      The player receiving the message.
     * @param doColour    If the method should format for colours or not.
     * @param doVariables If variables should be formatted or not.
     */
    public void broadcast(@NotNull final Player player, final boolean doColour, final boolean doVariables) {
        if (doVariables) variableFormat();
        if (doColour) colourFormat();
        if (this.message.endsWith(" -s") && this.canSilent) return;

        player.sendMessage(this.message);
    }

    /**
     * Sends a message to the console, the message is colour-formatted in case the console supports it and the placeholder
     * is directed to them.
     *
     * @param sender      The console sender.
     * @param doColour    If the method should format for colours or not.
     * @param doVariables If variables should be formatted or not.
     */
    public void broadcast(@NotNull final CommandSender sender, final boolean doColour, final boolean doVariables) {
        if (doVariables) variableFormat();
        if (doColour) colourFormat();
        if (this.message.endsWith(" -s") && this.canSilent) return;

        sender.sendMessage(this.message);
    }

    /**
     * If there is a value in the config that matches the id of this enum, that is returned. If not though, the default
     * value stored will be returned and a message is outputted to the console alerting of a missing value.
     *
     * @return The string from config that matches the value of id.
     */
    public String getString(String id, String normal) {
        String returning = EvenMoreFish.msgs.config.getString(id);
        if (returning != null) return returning;
        else {
            EvenMoreFish.logger.log(Level.SEVERE, "No value in messages.yml for: " + id + " using default value instead.");
            return normal;
        }
    }

    /**
     * If there is a value in the config that matches the id of this enum, that is returned. If not though, the default
     * value stored will be returned and a message is outputted to the console alerting of a missing value. This is for
     * string values however rather than just strings.
     *
     * @return The string list from config that matches the value of id.
     */
    public List<String> getStringList(String id, List<String> normal) {
        List<String> returning = EvenMoreFish.msgs.config.getStringList(id);
        if (!returning.isEmpty()) return returning;
        else {
            EvenMoreFish.logger.log(Level.SEVERE, "No value in messages.yml for: " + id + " using default value instead.");
            return normal;
        }
    }

    /**
     * This fetches the message straight from the messages.yml file and sends it back. It won't have been formatted unless
     * specified.
     *
     * @param doColour    If the method should format for colours or not.
     * @param doVariables If variables should be formatted or not.
     * @return The raw value from messages.yml or the raw value passed through.
     */
    public String getRawMessage(final boolean doColour, final boolean doVariables) {
        if (doVariables) variableFormat();
        if (doColour) colourFormat();

        if (this.canSilent && this.message.endsWith(" -s")) return "";
        else return this.message;
    }

    /**
     * Changes the message to a new one different to when the object was initialized, when it was initialized with a string.
     * All old variables and settings will remain the same but the message itself will change.
     *
     * @param message The new message to be displayed.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Changes the message to a new one different to when the object was initialized, when it was initialized with a string.
     * All old variables and settings will remain the same but the message itself will change.
     *
     * @param message The new message to be displayed.
     */
    public void setMessage(ConfigMessage message) {
        this.canSilent = message.isCanSilent();
        this.canHidePrefix = message.isCanHidePrefix();
        configMessageToString(message);
    }

    /**
     * Sets the message to use a specific prefix. This can only be done when the object was created using a string rather
     * than a ConfigMessage.
     *
     * @param type The type of prefix.
     */
    public void usePrefix(PrefixType type) {
        this.message = type.getPrefix() + this.message;
    }

    /**
     * Adds a variable to the object, when variables are being formatted they will be run through and replaced with their
     * proper values.
     *
     * @param code  The {variable} form of the variable.
     * @param value What the variable should be replaced with.
     */
    private void setVariable(@NotNull final String code, @NotNull final String value) {
        liveVariables.put(code, value);
    }

    /**
     * The colour used by the fish's rarity to apply a clean format for the fish, to replace the {rarity_colour} variable.
     *
     * @param colour The &_ code for the colour.
     */
    public void setRarityColour(@NotNull final String colour) {
        setVariable("{rarity_colour}", colour);
    }

    /**
     * The player's name to replace the {player} variable.
     *
     * @param playerName The name of the player.
     */
    public void setPlayer(@NotNull final String playerName) {
        setVariable("{player}", playerName);
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
     * Sets the competition type, checking against the values for each type stored in messages.yml to replace the {type}
     * variable.
     *
     * @param type The competition type.
     */
    public void setCompetitionType(@NotNull final CompetitionType type) {
        switch (type) {
            case MOST_FISH:
                setVariable("{type}", new Message(ConfigMessage.COMPETITION_TYPE_MOST).getRawMessage(false, false));
                break;
            case SPECIFIC_FISH:
                setVariable("{type}", new Message(ConfigMessage.COMPETITION_TYPE_SPECIFIC).getRawMessage(false, false));
                break;
            case SPECIFIC_RARITY:
                setVariable("{type}", new Message(ConfigMessage.COMPETITION_TYPE_SPECIFIC_RARITY).getRawMessage(false, false));
                break;
            default: setVariable("{type}", new Message(ConfigMessage.COMPETITION_TYPE_LARGEST).getRawMessage(false, false));
        }
    }

    /**
     * Goes through any custom lore stored in the fish's config, then creates a long string with line breaks to add to
     * the message instead of the {fish_lore} variable.
     *
     * @param id The fish's config section that would contain the custom lore.
     */
    public void setCustomFishLore(@NotNull final String id) {
        // custom lore in fish.yml
        List<String> potentialLore = EvenMoreFish.fishFile.getConfig().getStringList(id);

        // checks that the custom lore exists, then adds it on to the lore
        if (potentialLore.size() > 0) {
            StringBuilder customLore = new StringBuilder();
            // does colour coding, hence why .addAll() isn't used
            for (String line : potentialLore) {
                if (line.equals(potentialLore.get(potentialLore.size() - 1)))
                    customLore.append(FishUtils.translateHexColorCodes(line));
                else customLore.append(FishUtils.translateHexColorCodes(line)).append("\n");
            }
            // Replaces the fish lore with the value
            setVariable("{fish_lore}", customLore.toString());
        } else this.message = this.message.replace("\n{fish_lore}", "");
    }
}

