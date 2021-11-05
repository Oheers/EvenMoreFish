package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class Bar {

    String title;
    BossBar bar;

    String prefix;

    public Bar() {
        createBar();
    }

    public void timerUpdate(int timeLeft, int totalTime) {
        setTitle(timeLeft);
        setProgress(timeLeft, totalTime);
    }

    public void setProgress(int timeLeft, int totalTime) {
        double progress = (double) (timeLeft) / (double) (totalTime);

        if (progress < 0) {
            bar.setProgress(0.0D);
        } else if (progress > 1) {
            bar.setProgress(1.0D);
        } else {
            bar.setProgress(progress);
        }

    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setColour(BarColor colour) {
        this.bar.setColor(colour);
    }

    public void setTitle(int timeLeft) {
        bar.setTitle(prefix + ChatColor.RESET + FishUtils.timeFormat(timeLeft) + EvenMoreFish.msgs.getRemainingWord());
    }

    public void show() {
        for (Player p : Bukkit.getOnlinePlayers()) addPlayer(p);
        bar.setVisible(true);
    }

    public void hide() {
        for (Player p : Bukkit.getOnlinePlayers()) removePlayer(p);
        bar.setVisible(false);
    }

    public void createBar() {
        bar = Bukkit.getServer().createBossBar(title, BarColor.WHITE, BarStyle.SEGMENTED_10);
    }

    // Shows the bar to all players online
    public void renderBars() {
        bar.setVisible(true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bar.addPlayer(player);
        }
    }

    public void addPlayer(Player player) {
        bar.addPlayer(player);
    }

    public void removePlayer(Player player) {
        bar.removePlayer(player);
    }

    public void removeAllPlayers() {
        //bar.sr
    }
}
