package com.oheers.fish.competition;

import com.oheers.fish.FishUtils;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
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
        String typeString = switch (type) {
            case SPECIFIC_RARITY -> "Specific Rarity";
            case MOST_FISH -> "Most Fish";
            case LARGEST_FISH -> "Largest Fish";
            case LARGEST_TOTAL -> "Largest Total";
            case SPECIFIC_FISH -> "Specific Fish";
            case RANDOM -> "Random";
        };
        this.prefix = prefix.replace("{type}", typeString);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setColour(BarColor colour) {
        this.bar.setColor(colour);
    }

    public void setTitle(long timeLeft) {
        bar.setTitle(prefix + ChatColor.RESET + FishUtils.translateColorCodes(FishUtils.timeFormat(timeLeft) + ChatColor.RESET + new Message(ConfigMessage.BAR_REMAINING).getRawMessage(false)));
    }

    public void show() {
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
        bar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bar::addPlayer);
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
