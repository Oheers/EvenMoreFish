package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.scheduler.BukkitRunnable;

public class Ticker extends BukkitRunnable {

    Bar bar;

    public Ticker(Bar bar) {
        this.bar = bar;
    }


    @Override
    public void run() {
        if (!bar.timerUpdate()) {
            this.cancel();
            EvenMoreFish.active = null;
        }
    }

}
