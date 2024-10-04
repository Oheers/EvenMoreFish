package com.oheers.fish.competition.leaderboard;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.fishing.items.Fish;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Leaderboard implements LeaderboardHandler {

    CompetitionType type;
    List<CompetitionEntry> entries;

    public Leaderboard(CompetitionType type) {
        this.type = type;
        entries = new ArrayList<>();
    }

    @Override
    public List<CompetitionEntry> getEntries() {
        Comparator<CompetitionEntry> entryComparator = type.shouldReverseLeaderboard() ?
                (e1, e2) -> Float.compare(e1.getValue(), e2.getValue()) :
                (e1, e2) -> Float.compare(e2.getValue(), e1.getValue());
        return entries.stream()
                .sorted(entryComparator)
                .toList();
    }

    @Override
    public void addEntry(UUID player, Fish fish) {
        CompetitionEntry entry = new CompetitionEntry(player, fish, type);
        entries.add(entry);
    }

    @Override
    public void addEntry(CompetitionEntry entry) {
        entries.add(entry);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public boolean contains(CompetitionEntry entry) {
        return entries.contains(entry);
    }

    @Override
    public CompetitionEntry getEntry(UUID player) {
        // Does not need to use the sorted list
        for (CompetitionEntry entry : entries) {
            if (entry.getPlayer().equals(player)) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public CompetitionEntry getEntry(int place) {
        try {
            // Needs to use the sorted list
            return getEntries().get(place - 1);
        } catch (IndexOutOfBoundsException exception) {
            return null;
        }
    }

    @Override
    public int getSize() {
        return entries.size();
    }

    @Override
    public boolean hasEntry(UUID player) {
        // Does not need to use the sorted list
        for (CompetitionEntry entry : entries) {
            if (entry.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeEntry(CompetitionEntry entry) {
        entries.remove(entry);
    }

    @Override
    public CompetitionEntry getTopEntry() {
        // Needs to use the sorted list
        return getEntries().get(0);
    }

}
