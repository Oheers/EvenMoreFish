package com.oheers.fish.competition;

import com.oheers.fish.competition.leaderboard.LeaderboardHandler;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface CompetitionType {

    String getIdentifier();

    String getAuthor();

    Plugin getPlugin();

    LeaderboardHandler getNewLeaderboard();

    default boolean register() {
        return CompetitionManager.getInstance().registerType(this);
    }

    @Nullable Consumer<Competition> getTypeBeginLogic();

}
