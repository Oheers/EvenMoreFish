package com.oheers.fish.competition;

import org.bukkit.scheduler.BukkitRunnable;

public class Ticker extends BukkitRunnable {

    Bar bar;

    public Ticker(Bar bar) {
        this.bar = bar;
    }


    @Override
    public void run() {
        bar.timerUpdate();
    }

}
