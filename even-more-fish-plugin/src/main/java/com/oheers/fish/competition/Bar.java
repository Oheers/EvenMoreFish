package com.oheers.fish.competition;

import com.oheers.fish.FishUtils;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.api.adapter.AbstractMessage;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class Bar {

    String title;
    BossBar bar;
    private boolean shouldShow = true;

    String prefix;

    public Bar() {
        createBar();
    }

    public void setShouldShow(boolean shouldShow) {
        this.shouldShow = shouldShow;
    }

    public void timerUpdate(long timeLeft, long totalTime) {
        setTitle(timeLeft);
        setProgress(timeLeft, totalTime);
    }

    public void setProgress(long timeLeft, long totalTime) {
        double progress = (double) (timeLeft) / (double) (totalTime);

        if (progress < 0) {
            progress = 0.0D;
        } else if (progress > 1) {
            progress = 1.0D;
        }
        bar.setProgress(progress);

    }

    public void setPrefix(String prefix, CompetitionType type) {
        this.prefix = prefix.replace("{type}", type.getBarPrefix());
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setColour(BarColor colour) {
        this.bar.setColor(colour);
    }

    public void setTitle(long timeLeft) {
        AbstractMessage layoutMessage = ConfigMessage.BAR_LAYOUT.getMessage();
        layoutMessage.setVariable("{prefix}", prefix);
        layoutMessage.setVariable("{time-formatted}", FishUtils.translateColorCodes(FishUtils.timeFormat(timeLeft)));
        layoutMessage.setVariable("{remaining}", ConfigMessage.BAR_REMAINING.getMessage().getRawMessage());
        bar.setTitle(layoutMessage.getRawMessage());
    }

    public void show() {
        if (!shouldShow) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(this::addPlayer);
        bar.setVisible(true);
    }

    public void hide() {
        Bukkit.getOnlinePlayers().forEach(this::removePlayer);
        bar.setVisible(false);
    }

    public void createBar() {
        BarStyle barStyle = MainConfig.getInstance().getBarStyle();
        bar = Bukkit.getServer().createBossBar(title, BarColor.WHITE, barStyle);
    }

    // Shows the bar to all players online
    public void renderBars() {
        if (!shouldShow) {
            return;
        }
        bar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bar::addPlayer);
    }

    public void addPlayer(Player player) {
        if (!shouldShow) {
            return;
        }
        bar.addPlayer(player);
    }

    public void removePlayer(Player player) {
        bar.removePlayer(player);
    }

    public void removeAllPlayers() {
        bar.removeAll();
    }
}
