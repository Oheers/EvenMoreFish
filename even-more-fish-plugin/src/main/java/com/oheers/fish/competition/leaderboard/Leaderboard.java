package com.oheers.fish.competition.leaderboard;

import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.fishing.items.Fish;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Leaderboard implements LeaderboardHandler {

    private final CompetitionType type;
    private final List<CompetitionEntry> entries;

    public Leaderboard(CompetitionType type) {
        this.type = type;
        this.entries = new ArrayList<>();
    }

    @Override
    public List<CompetitionEntry> getEntries() {
        Comparator<CompetitionEntry> entryComparator = type.shouldReverseLeaderboard() ?
                Comparator.comparingDouble(CompetitionEntry::getValue) :
                Comparator.comparingDouble(CompetitionEntry::getValue).reversed();

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
        return getEntry(player) != null;
    }

    @Override
    public void removeEntry(CompetitionEntry entry) {
        entries.remove(entry);
    }

    @Override
    public CompetitionEntry getTopEntry() {
        return getEntries().isEmpty() ? null : getEntries().get(0);
    }

    /**
     * Updates an entry in the leaderboard by removing it, applying the changes, and re-adding it.
     *
     * @param entry The updated entry.
     */
    public void updateEntry(CompetitionEntry entry) {
        removeEntry(entry); // Remove the current entry
        addEntry(entry);    // Add the updated entry
    }
}
