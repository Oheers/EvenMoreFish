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

    public Bar() {
        createBar();
        renderBars();
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

    public void setTitle(int timeLeft) {
        bar.setTitle(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getBarPrefix()) + ChatColor.RESET + FishUtils.timeFormat(timeLeft) + EvenMoreFish.msgs.getRemainingWord());
    }

    private void show() {
        bar.setVisible(true);
    }

    private void hide() {
        bar.setVisible(false);
    }

    public void createBar() {
        BarColor bC = BarColor.valueOf(EvenMoreFish.mainConfig.getBossbarColour());
        if (bC == null) bC = BarColor.GREEN;

        bar = Bukkit.getServer().createBossBar(title, bC, BarStyle.SEGMENTED_10);
    }

    // Shows the bar to all players online
    private void renderBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            bar.addPlayer(player);
        }
        show();
    }

    public void addPlayer(Player player) {
        bar.addPlayer(player);
    }

    public void removePlayer(Player player) {
        bar.removePlayer(player);
    }

    public void removeAllPlayers() {
        bar.removeAll();
    }
}
