package com.oheers.fish.competition;

import com.oheers.fish.fishing.items.Fish;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

interface LeaderboardHandler {

    Set<CompetitionEntry> getEntries();

    void addEntry(UUID player, Fish fish);

    void addEntry(CompetitionEntry entry);

    void clear();

    boolean contains(CompetitionEntry entry);

    CompetitionEntry getEntry(UUID player);

    int getSize();

    boolean hasEntry(UUID player);

    void removeEntry(CompetitionEntry entry) throws Exception;

    CompetitionEntry getTopEntry();

    UUID getPlayer(int i);

    float getPlaceValue(int i);

    Fish getPlaceFish(int i);

}

public class Leaderboard implements LeaderboardHandler {

    CompetitionType type;
    TreeSet<CompetitionEntry> entries;

    Leaderboard(CompetitionType type) {
        this.type = type;
        entries = new TreeSet<>();
    }

    @Override
    public Set<CompetitionEntry> getEntries() {
        return entries;
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
    public int getSize() {
        return entries.size();
    }

    @Override
    public boolean hasEntry(UUID player) {
        for (CompetitionEntry entry : entries) {
            if (entry.getPlayer() == player) {
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
        return entries.first();
    }

    @Override
    public UUID getPlayer(int i) {
        int count = 1;
        for (CompetitionEntry entry : entries) {
            if (count == i) {
                return entry.getPlayer();
            }
            count++;
        }
        return null;
    }

    @Override
    public float getPlaceValue(int i) {
        int count = 1;
        for (CompetitionEntry entry : entries) {
            if (count == i) {
                return entry.getValue();
            }
            count++;
        }
        return -1.0f;
    }

    @Override
    public Fish getPlaceFish(int i) {
        int count = 1;
        for (CompetitionEntry entry : entries) {
            if (count == i) {
                return entry.getFish();
            }
            count++;
        }
        return null;
    }
}
