package com.oheers.fish.database.model;

import com.oheers.fish.fishing.items.Fish;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FishReport {
    private final String name;
    private final String rarity;
    private int numCaught;
    private float size;
    private final LocalDateTime localDateTime;

    public FishReport(String rarity, String name, float size, int numCaught, long timeEpoch) {
        this.rarity = rarity;
        this.name = name;
        this.numCaught = numCaught;
        this.size = size;
        this.localDateTime = getLocalDateTimeFromEpoch(timeEpoch);
    }

    public FishReport(String rarity, String name, float size, int numCaught, LocalDateTime time) {
        this.rarity = rarity;
        this.name = name;
        this.numCaught = numCaught;
        this.size = size;
        this.localDateTime = time;
    }



    private LocalDateTime getLocalDateTimeFromEpoch(long timeEpoch) {
        if (timeEpoch == -1)
            return LocalDateTime.now();
        return Instant.ofEpochSecond(timeEpoch)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
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
        return localDateTime.atZone(ZoneId.systemDefault()) // Use system's default time zone
                .toEpochSecond();
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void addFish(final @NotNull Fish fish) {
        if (fish.getLength() > this.size) {
            this.size = fish.getLength();
        }
        numCaught++;
    }

    @Override
    public String toString() {
        return "FishReport{" +
                "name='" + name + '\'' +
                ", rarity='" + rarity + '\'' +
                ", numCaught=" + numCaught +
                ", size=" + size +
                ", localDateTime=" + localDateTime +
                '}';
    }

}
