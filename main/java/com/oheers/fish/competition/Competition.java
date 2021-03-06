package com.oheers.fish.competition;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Competition {

    // fisher, fish length
    HashMap<Player, Float> leaderboard = new HashMap<>();

    Bar bar;

    // (seconds) the length of the competition
    int duration;

    public Competition(int duration) {
        this.duration = duration;
        init();
    }

    private void init() {
        bar = new Bar(this.duration);
    }

}
