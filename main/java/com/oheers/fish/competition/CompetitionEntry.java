package com.oheers.fish.competition;

import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;

public class CompetitionEntry {

    Float fishLength;
    Fish fish;
    Player fisher;

    public CompetitionEntry(Float fishLength, Fish fish, Player fisher) {
        this.fishLength = fishLength;
        this.fish = fish;
        this.fisher = fisher;
    }

    public Float getFishLength() {
        return this.fishLength;
    }

    public Fish getFish() {
        return this.fish;
    }

    public Player getFisher() {
        return this.fisher;
    }
}
