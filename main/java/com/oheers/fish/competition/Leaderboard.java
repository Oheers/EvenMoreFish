package com.oheers.fish.competition;

import com.oheers.fish.fishing.items.Fish;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

class CompetitionEntry implements Comparable<CompetitionEntry> {

	private long time;
	private final UUID player;
	private final Fish fish;
	private float value;

	CompetitionEntry(UUID player, Fish fish, CompetitionType type) {
		this.player = player;
		this.fish = fish;
		this.time = Instant.now().toEpochMilli();

		if (type == CompetitionType.LARGEST_FISH) this.value = fish.getLength();
		else this.value = 1;
	}

	public void incrementValue() {
		this.value++;
		this.time = Instant.now().toEpochMilli();
	}

	public void setValue(float value) {
		this.value = value;
	}

	public Fish getFish() {
		return fish;
	}

	public long getTime() {
		return time;
	}

	public int getHash() {
		return this.hashCode();
	}

	public float getValue() {
		return value;
	}

	public UUID getPlayer() {
		return player;
	}

	@Override
	public int compareTo(@NotNull CompetitionEntry entry) {
		// if o's value and this's value are equal, use the time caught instead
		if (entry.getValue() != this.value) {
			return (entry.getValue() > this.value) ? 1 : -1;
		} else {
			return (entry.getTime() > this.time) ? 1 : -1;
		}
	}


	@Override
	public String toString() {
		return "CompetitionEntry[" + this.player + ", " + value + ", " + time + "]";
	}
}

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
	public Iterator<CompetitionEntry> getIterator() {
		return entries.iterator();
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
