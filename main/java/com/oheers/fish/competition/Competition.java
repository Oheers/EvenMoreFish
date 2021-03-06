package com.oheers.fish.competition;
import com.oheers.fish.EvenMoreFish;
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
    }

    public void start() {
        bar = new Bar(this.duration);
        EvenMoreFish.active = this;
    }

    public Bar getBar() {
        return bar;
    }
}
