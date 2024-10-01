package com.oheers.fish.competition;

import com.oheers.fish.fishing.items.Fish;

import java.time.Instant;
import java.util.UUID;

public class CompetitionEntry {

    private final UUID player;
    private final Fish fish;
    private long time;
    private float value;

    public CompetitionEntry(UUID player, Fish fish, CompetitionType type) {
        this.player = player;
        this.fish = fish;
        this.time = Instant.now().toEpochMilli();

        if (type.shouldUseLength()) {
            this.value = fish.getLength();
        } else {
            this.value = 1;
        }
    }

    /**
     * Increases the player's "score" by a set amount. The time that the entry was made will always be set to the current
     * epoch millisecond and will be unaffected by an increaseAmount that is not 1.
     *
     * @param increaseAmount The amount to increase the player's score by.
     */
    public void incrementValue(float increaseAmount) {
        this.value += Math.abs(increaseAmount);
        this.time = Instant.now().toEpochMilli();
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

    public void setValue(float value) {
        this.value = value;
    }

    public UUID getPlayer() {
        return player;
    }


    @Override
    public String toString() {
        return "CompetitionEntry[" + this.player + ", " + value + ", " + time + "]";
    }
}
