package com.oheers.fish.competition;


import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import org.jetbrains.annotations.NotNull;

public interface CompetitionStrategy {
    boolean begin(Competition competition);
    void applyLeaderboard();
    void applyConsoleLeaderboard();
    void sendPlayerLeaderboard();
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
        message.setCompetitionType(competition.competitionType);
        return message;
    }

}