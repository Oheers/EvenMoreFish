package com.oheers.fish.competition;

import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;

public class Record {

    Fish fish;
    Player fisherman;

    public Record(Fish fish, Player fisherman) {
        this.fish = fish;
        this.fisherman = fisherman;
    }

    public Fish getFish() {
        return fish;
    }

    public Player getFisherman() {
        return fisherman;
    }
}
