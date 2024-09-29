package com.oheers.fish.competition.leaderboard;

import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.fishing.items.Fish;

import java.util.*;

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

}

