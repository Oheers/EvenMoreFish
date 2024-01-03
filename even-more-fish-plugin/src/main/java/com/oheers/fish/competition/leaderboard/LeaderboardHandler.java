package com.oheers.fish.competition.leaderboard;

import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.fishing.items.Fish;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

interface LeaderboardHandler {

    Set<CompetitionEntry> getEntries();

    void addEntry(UUID player, Fish fish);

    void addEntry(CompetitionEntry entry);

    void clear();

    boolean contains(CompetitionEntry entry);

    CompetitionEntry getEntry(UUID player);

    Iterator<CompetitionEntry> getIterator();

    int getSize();

    boolean hasEntry(UUID player);

    void removeEntry(CompetitionEntry entry) throws Exception;

    CompetitionEntry getTopEntry();

    UUID getPlayer(int i);

    float getPlaceValue(int i);

    Fish getPlaceFish(int i);

}
