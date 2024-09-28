package com.oheers.fish.competition;

import com.oheers.fish.fishing.items.Fish;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

interface LeaderboardHandler {

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

public class Leaderboard implements LeaderboardHandler {

    CompetitionType type;
    List<CompetitionEntry> entries;

    Leaderboard(CompetitionType type) {
        this.type = type;
        entries = new ArrayList<>();
    }

    @Override
    public List<CompetitionEntry> getEntries() {
        // Sort the list so the highest entry value is first
        switch (type) {
            case SHORTEST_FISH, SHORTEST_TOTAL -> {
                return entries.stream()
                        .sorted((e1, e2) -> Float.compare(e1.getValue(), e2.getValue()))
                        .toList();
            }
            default -> {
                return entries.stream()
                        .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()))
                        .toList();
            }
        }
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
        entries.removeIf(e -> e == entry);
    }

    @Override
    public CompetitionEntry getTopEntry() {
        // Needs to use the sorted list
        return getEntries().get(0);
    }

}
