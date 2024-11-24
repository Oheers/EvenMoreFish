package com.oheers.fish.competition;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

/**
 * This interface defines the behavior for competition strategies.
 */
public interface CompetitionStrategy {

    /**
     * Begins the competition.
     *
     * @param competition The competition to begin.
     * @return True if the competition was successfully started, false otherwise.
     */
    default boolean begin(Competition competition) {
        return true;
    }

    /**
     * Applies the competition to the leaderboard.
     *
     * @param fish       The fish caught during the competition.
     * @param fisher     The player who caught the fish.
     * @param leaderboard The leaderboard to apply the competition to.
     * @param competition The competition being applied.
     */
    void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition);

    /**
     * Gets the begin message for the competition.
     *
     * @param competition The competition to get the begin message for.
     * @param type        The type of competition.
     * @return The begin message for the competition.
     */
    default Message getBeginMessage(Competition competition, CompetitionType type) {
        Message message = new Message(ConfigMessage.COMPETITION_START);
        message.setCompetitionType(type);
        return message;
    }

    /**
     * Gets the single console leaderboard message.
     *
     * @param message The message to set the leaderboard information on.
     * @param entry   The competition entry to get the leaderboard information from.
     * @return The single console leaderboard message.
     */
    default Message getSingleConsoleLeaderboardMessage(@NotNull Message message, @NotNull CompetitionEntry entry) {
        //todo temp, since this really isn't supposed to be the case, but was the original code. idk
        message.setMessage(ConfigMessage.LEADERBOARD_MOST_FISH);
        message.setAmount(Integer.toString((int) entry.getValue()));
        return message;
    }

    /**
     * Gets the single player leaderboard message.
     *
     * @param message The message to set the leaderboard information on.
     * @param entry   The competition entry to get the leaderboard information from.
     * @return The single player leaderboard message.
     */
    default Message getSinglePlayerLeaderboard(@NotNull Message message, @NotNull CompetitionEntry entry) {
        //todo temp, since this really isn't supposed to be the case, but was the original code. idk
        message.setMessage(ConfigMessage.LEADERBOARD_MOST_FISH);
        message.setAmount(Integer.toString((int) entry.getValue()));
        return message;
    }

    /**
     * This creates a message object and applies all the settings to it to make it able to use the {type} variable. It
     * takes into consideration whether it's a specific fish/rarity competition.
     *
     * @param competition   The competition to get the message for.
     * @param configMessage The configmessage to use. Must have the {type} variable in it.
     * @return A message object that's pre-set to be compatible for the time remaining.
     */
    default Message getTypeFormat(@NotNull Competition competition, ConfigMessage configMessage) {
        Message message = new Message(configMessage);
        message.setTimeFormatted(FishUtils.timeFormat(competition.timeLeft));
        message.setTimeRaw(FishUtils.timeRaw(competition.timeLeft));
        message.setCompetitionType(competition.getCompetitionType());
        return message;
    }

    default DecimalFormat getDecimalFormat() {
        return new DecimalFormat("#.0"); // For 1 decimal place
    }

}