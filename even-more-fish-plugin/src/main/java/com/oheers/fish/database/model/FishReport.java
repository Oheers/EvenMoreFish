package com.oheers.fish.database.model;

import com.oheers.fish.fishing.items.Fish;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class FishReport {

    private final String rarity;
    private final String name;
    private int numCaught;
    private final long timeEpoch;
    private float size;

    public FishReport(String rarity, String name, float size, int numCaught, long timeEpoch) {
        this.rarity = rarity;
        this.name = name;
        this.numCaught = numCaught;
        this.size = size;
        this.timeEpoch = calcTimeEpoch(timeEpoch);
    }
    
    private long calcTimeEpoch(long timeEpoch) {
        if (timeEpoch == -1)
            return Instant.now().getEpochSecond();
        return timeEpoch;
    }

    public int getNumCaught() {
        return numCaught;
    }

    public void setNumCaught(int numCaught) {
        this.numCaught = numCaught;
    }

    public String getRarity() {
        return rarity;
    }

    public String getName() {
        return name;
    }

    public float getLargestLength() {
        return size;
    }

    public void setLargestLength(float largestLength) {
        this.size = largestLength;
    }

    public long getTimeEpoch() {
        return timeEpoch;
    }

    public void addFish(final @NotNull Fish fish) {
        if (fish.getLength() > this.size) {
            this.size = fish.getLength();
        }
        numCaught++;
    }

    @Override
    public String toString() {
        return "FishReport=[name:" + name + ", rarity:" + rarity + ", largestLength:" + size + ", numCaught:" + numCaught + "]";
    }
}
