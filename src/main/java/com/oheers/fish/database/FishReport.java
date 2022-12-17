package com.oheers.fish.database;

import com.oheers.fish.fishing.items.Fish;

import java.time.Instant;

public class FishReport {

    String r, n;
    int c;
    long t;
    float l;

    public FishReport(String rarity, String name, float size, int numCaught, long timeEpoch) {
        this.r = rarity;
        this.n = name;
        this.c = numCaught;
        this.l = size;
        if (timeEpoch == -1) this.t = Instant.now().getEpochSecond();
        else this.t = timeEpoch;
    }

    public int getNumCaught() {
        return c;
    }

    public void setNumCaught(int numCaught) {
        this.c = numCaught;
    }

    public String getRarity() {
        return r;
    }

    public String getName() {
        return n;
    }

    public float getLargestLength() {
        return l;
    }

    public void setLargestLength(float largestLength) {
        this.l = largestLength;
    }

    public long getTimeEpoch() {
        return t;
    }

    public void addFish(Fish f) {
        if (f.getLength() > this.l) {
            this.l = f.getLength();
        }
        c++;
    }

    @Override
    public String toString() {
        return "FishReport=[name:" + n + ", rarity:" + r + ", largestLength:" + l + ", numCaught:" + c;
    }
}
