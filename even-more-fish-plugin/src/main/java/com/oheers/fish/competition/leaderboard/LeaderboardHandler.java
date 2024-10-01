package com.oheers.fish.competition.leaderboard;

import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface LeaderboardHandler {

    List<CompetitionEntry> getEntries();

    void addEntry(UUID player, Fish fish);

    void addEntry(CompetitionEntry entry);

    void clear();

    boolean contains(CompetitionEntry entry);

    CompetitionEntry getEntry(UUID player);

    CompetitionEntry getEntry(int place);

    int getSize();

    boolean hasEntry(UUID player);

    void removeEntry(CompetitionEntry entry) throws Exception;

    CompetitionEntry getTopEntry();

    void send(@NotNull CommandSender sender);

    default void sendToConsole() {
        send(Bukkit.getConsoleSender());
    }

    default void sendToAll() {
        Bukkit.getOnlinePlayers().forEach(this::send);
        sendToConsole();
    }

}

