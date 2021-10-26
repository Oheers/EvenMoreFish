package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.FishReport;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JoinChecker implements Listener {

    // Gives the player the active fishing bar if there's a fishing event cracking off
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Competition.isActive()) {
            EvenMoreFish.active.getStatusBar().addPlayer(event.getPlayer());
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EvenMoreFish.getPlugin(EvenMoreFish.class),
                    () -> event.getPlayer().sendMessage(EvenMoreFish.active.getStartMessage().setMSG(EvenMoreFish.msgs.getCompetitionJoin()).toString()), 20*3);
        }

        List<FishReport> reports;

        if (Database.hasUser(event.getPlayer().getUniqueId().toString())) {
            reports = Database.readUserData(event.getPlayer().getUniqueId().toString());
            if (reports != null) {
                System.out.println("reports == " + Arrays.toString(reports.toArray()));
            } else {
                System.out.println("null");
            }
        } else {
            reports = new ArrayList<>();
            Database.addUser(event.getPlayer().getUniqueId().toString(), reports);
        }

        EvenMoreFish.fishReports.put(event.getPlayer().getUniqueId(), reports);
    }

    // Removes the player from the bar list if they leave the server
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        for (UUID u : EvenMoreFish.fishReports.keySet()) {
            System.out.println("u: " + u.toString());
            for (FishReport fR : EvenMoreFish.fishReports.get(u)) {
                System.out.println("fR: " + fR.toString());
            }
        }

        if (Competition.isActive()) {
            EvenMoreFish.active.getStatusBar().removePlayer(event.getPlayer());
        }

        if (EvenMoreFish.fishReports.containsKey(event.getPlayer().getUniqueId())) {
            if (Database.hasUser(event.getPlayer().getUniqueId().toString())) {
                Database.writeUserData(event.getPlayer().getUniqueId().toString(), EvenMoreFish.fishReports.get(event.getPlayer().getUniqueId()));
            } else {
                Database.addUser(event.getPlayer().getUniqueId().toString(), new ArrayList<>());
            }

            EvenMoreFish.fishReports.remove(event.getPlayer().getUniqueId());
        }
    }
}
