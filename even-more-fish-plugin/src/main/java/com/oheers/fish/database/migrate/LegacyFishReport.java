package com.oheers.fish.database.migrate;


import java.time.Instant;
/*
  {
    "r": "Common",
    "n": "Stick",
    "c": 1,
    "t": 1718461602,
    "l": 16.6
  }
 */
public class LegacyFishReport {
    private final String r; //Rarity
    private final String n; //Name
    private int c; //count
    private final long t; //time
    private float l; //length

    public LegacyFishReport(String r, String n, float l, int c, long t) {
        this.r = r;
        this.n = n;
        this.c = c;
        this.l = l;
        this.t = calcTimeEpoch(t);
    }

    private long calcTimeEpoch(long timeEpoch) {
        if (timeEpoch == -1)
            return Instant.now().getEpochSecond();
        return timeEpoch;
    }

    public long getTimeEpoch() {
        return t;
    }

    public String getRarity() {
        return r;
    }

    public String getName() {
        return n;
    }

    public int getNumCaught() {
        return c;
    }

    public float getLargestLength() {
        return l;
    }

    @Override
    public String toString() {
        return "LegacyFishReport{" +
                "rarity='" + r + '\'' +
                ", name='" + n + '\'' +
                ", numCaught=" + c +
                ", time=" + t +
                ", length=" + l +
                '}';
    }
}
