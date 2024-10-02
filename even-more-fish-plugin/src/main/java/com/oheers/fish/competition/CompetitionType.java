package com.oheers.fish.competition;

import com.oheers.fish.competition.leaderboard.LeaderboardHandler;
import com.oheers.fish.config.messages.Message;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface CompetitionType {

    /**
     * @return The identifier for this CompetitionType, used in configs.
     */
    @NotNull String getIdentifier();

    /**
     * @return The author of this CompetitionType, used in the command.
     */
    @NotNull String getAuthor();

    /**
     * @return The plugin responsible for this CompetitionType
     */
    @NotNull Plugin getPlugin();

    /**
     * @return A new leaderboard instance for use in different competitions.
     */
    @Nullable LeaderboardHandler createLeaderboard();

    @NotNull Message getMessageTypeVariable();

    /**
     * Registers this CompetitionType with the CompetitionManager
     * @return false if a type with this identifier already exists
     */
    default boolean register() {
        return CompetitionManager.getInstance().registerType(this);
    }

    /**
     * Executed near the start of the competition.
     * This can be used to, for example, stop the competition if a valid fish is not found for it.
     * @return A function to act on the Competition instance, returns false if the competition should not start.
     */
    @Nullable Function<Competition, @NotNull Boolean> getTypeBeginLogic();

    @Nullable Consumer<Message> getTypeFormatLogic();

    @NotNull Message getCompetitionStartMessage();

    boolean shouldUseLength();

    String getBarPrefix();

}
