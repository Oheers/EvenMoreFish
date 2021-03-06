package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class Bar {

    String title;
    BossBar bar;

    int timeLeft;
    int totalTime;

    public Bar(int totalTime) {
        this.totalTime = totalTime;

        createBar();
        renderBars();
        begin();
    }

    public void timerUpdate() {
        if (checkEnd()) {
            timeLeft--;
            setTitle();
            setProgress();
        }
    }

    private void begin() {
        Ticker ticker = new Ticker(this);
        ticker.runTaskTimer(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), 0, 20);
    }

    private void setProgress() {
        double progress = (double) (timeLeft) / (double) (totalTime);

        if (progress < 0) {
            bar.setProgress(0.0D);
        } else if (progress > 1) {
            bar.setProgress(1.0D);
        } else {
            bar.setProgress(progress);
        }

    }

    private void setTitle() {
        String returning = "";

        if (timeLeft >= 3600) {
            returning += (timeLeft/3600) + " hours ";
        }

        if (timeLeft%3600 != 0) {
            returning += (timeLeft%3600) + " minutes and ";
        }

        // I always want the remaining seconds to show, e.g. 1 minutes and 0 seconds
        returning += (timeLeft%60) + " seconds";

        bar.setTitle(returning);
    }

    private void show() {
        bar.setVisible(true);
    }

    private void hide() {
        bar.setVisible(false);
    }

    public void createBar() {
        bar = Bukkit.getServer().createBossBar(title, BarColor.BLUE, BarStyle.SEGMENTED_10);
    }

    // Shows the bar to all players online
    private void renderBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            bar.addPlayer(player);
        }
        show();
    }

    // Checks if there's 0 seconds left on the timer
    private boolean checkEnd() {
        if (timeLeft == 0) {
            hide();
            return false;
        } else {
            return true;
        }
    }
}
