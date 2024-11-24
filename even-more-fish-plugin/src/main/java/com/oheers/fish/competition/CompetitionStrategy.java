package com.oheers.fish.competition;


import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface CompetitionStrategy {
    default boolean begin(Competition competition) {
        return true;
    }

    void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition);

    default Message getSingleConsoleLeaderboardMessage(Message message, CompetitionEntry entry) {
        //todo temp, since this really isn't supposed to be the case, but was the original code. idk
        message.setMessage(ConfigMessage.LEADERBOARD_MOST_FISH);
        message.setAmount(Integer.toString((int) entry.getValue()));
        return message;
    }

    default Message getBeginMessage(Competition competition, CompetitionType type) {
        Message message = new Message(ConfigMessage.COMPETITION_START);
        message.setCompetitionType(type);
        return message;
    }

    default Message getSinglePlayerLeaderboard(Message message, CompetitionEntry entry) {
        //todo temp, since this really isn't supposed to be the case, but was the original code. idk
        message.setMessage(ConfigMessage.LEADERBOARD_MOST_FISH);
        message.setAmount(Integer.toString((int) entry.getValue()));
        return message;
    }

    /**
     * This creates a message object and applies all the settings to it to make it able to use the {type} variable. It
     * takes into consideration whether it's a specific fish/rarity competition.
     *
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

}