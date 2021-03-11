package com.oheers.fish.competition;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        announce();
        bar = new Bar(this.duration);
        EvenMoreFish.active = this;
    }

    public Bar getBar() {
        return bar;
    }

    private void announce() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.competitionStart));
        }
    }
}
