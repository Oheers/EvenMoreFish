package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.FishReport;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class JoinChecker implements Listener {

    // Gives the player the active fishing bar if there's a fishing event cracking off
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Competition.isActive()) {
            EvenMoreFish.active.getStatusBar().addPlayer(event.getPlayer());
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EvenMoreFish.getPlugin(EvenMoreFish.class),
                    () -> event.getPlayer().sendMessage(EvenMoreFish.active.getStartMessage().setMSG(EvenMoreFish.msgs.getCompetitionJoin()).toString()), 20*3);
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                if (EvenMoreFish.mainConfig.isDatabaseOnline()) {
                    List<FishReport> reports = new ArrayList<>();

                    if (Database.hasFlatFile(event.getPlayer().getUniqueId().toString())) {
                        try {
                            reports = Database.readUserData(event.getPlayer().getUniqueId().toString());
                        } catch (FileNotFoundException e) {
                            EvenMoreFish.logger.log(Level.SEVERE, "Failed to check existence of user file in /data/ for: " + event.getPlayer().getUniqueId());
                            e.printStackTrace();
                        }
                    }

                    if (reports != null) {
                        EvenMoreFish.fishReports.put(event.getPlayer().getUniqueId(), reports);
                    } else {
                        EvenMoreFish.logger.log(Level.SEVERE, "Fetched a null reports file for: " + event.getPlayer().getUniqueId());
                    }

                    if (!Database.hasUser(event.getPlayer().getUniqueId().toString())) {
                        Database.addUser(event.getPlayer().getUniqueId().toString());
                    }
                }
            }
        }.runTaskAsynchronously(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class));
    }

    // Removes the player from the bar list if they leave the server
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        EvenMoreFish.logger.log(Level.INFO, "Quit event triggered for: @" + event.getPlayer().getName() + ".");

        if (Competition.isActive()) {
            EvenMoreFish.active.getStatusBar().removePlayer(event.getPlayer());
        }

        if (EvenMoreFish.mainConfig.isDatabaseOnline()) {
            EvenMoreFish.logger.log(Level.INFO, "Database is online.");

            new BukkitRunnable() {

                @Override
                public void run() {
                    if (!Database.hasUser(event.getPlayer().getUniqueId().toString())) {
                        EvenMoreFish.logger.log(Level.INFO, "User not present in SQL database, adding.");
                        Database.addUser(event.getPlayer().getUniqueId().toString());
                    }

                    if (EvenMoreFish.fishReports.containsKey(event.getPlayer().getUniqueId())) {
                        EvenMoreFish.logger.log(Level.INFO, "User present in fish-reports list.");
                        Database.writeUserData(event.getPlayer().getUniqueId().toString(), EvenMoreFish.fishReports.get(event.getPlayer().getUniqueId()));
                    }

                    EvenMoreFish.fishReports.remove(event.getPlayer().getUniqueId());
                }
            }.runTaskAsynchronously(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class));

        }
    }
}
