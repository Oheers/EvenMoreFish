package com.oheers.fish.commands.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Start {

    public static void run(int args, Player player) {
        int duration;

        try {
            duration = args;
            Competition comp = new Competition(duration);
            comp.start();
        } catch (NumberFormatException nfe) {
            player.sendMessage("Not a number");
        }
    }
}
