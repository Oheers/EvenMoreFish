package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.database.FishReport;
import com.oheers.fish.database.Table;
import com.oheers.fish.database.UserReport;
import com.oheers.fish.exceptions.InvalidTableException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class JoinChecker implements Listener {

    // Gives the player the active fishing bar if there's a fishing event cracking off
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Competition.isActive()) {
            EvenMoreFish.active.getStatusBar().addPlayer(event.getPlayer());
            EvenMoreFish.active.getStartMessage().setMessage(ConfigMessage.COMPETITION_JOIN);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EvenMoreFish.getPlugin(EvenMoreFish.class),
                    () -> EvenMoreFish.active.getStartMessage().broadcast(event.getPlayer(), true, true), 20*3);
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                if (EvenMoreFish.mainConfig.isDatabaseOnline()) {
                    List<FishReport> reports = new ArrayList<>();

                    try {
                        if (EvenMoreFish.databaseV3.hasUser(event.getPlayer().getUniqueId(), Table.EMF_FISH_LOG, true)) {
                            reports = EvenMoreFish.databaseV3.getFishReports(event.getPlayer().getUniqueId());
                        } else {
                            reports = new ArrayList<>();
                            EvenMoreFish.logger.log(Level.INFO, event.getPlayer().getName() + " has joined for the first time, creating new data handle for them.");
                        }
                    } catch (SQLException | InvalidTableException exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Failed to check database exitence of user " + event.getPlayer().getUniqueId());
                    }

                    if (reports != null) {
                        EvenMoreFish.fishReports.put(event.getPlayer().getUniqueId(), reports);
                    } else {
                        EvenMoreFish.logger.log(Level.SEVERE, "Fetched a null reports file for: " + event.getPlayer().getUniqueId());
                    }

                    try {
                        UserReport report = EvenMoreFish.databaseV3.readUserReport(event.getPlayer().getUniqueId());
                        if (report == null) {
                            EvenMoreFish.databaseV3.createUser(event.getPlayer().getUniqueId());
                            report = EvenMoreFish.databaseV3.readUserReport(event.getPlayer().getUniqueId());
                            if (report == null) {
                                EvenMoreFish.logger.log(Level.SEVERE, "Failed to create new empty user report for " + event.getPlayer().getName());
                            } else {
                                EvenMoreFish.userReports.put(event.getPlayer().getUniqueId(), report);
                            }
                        } else {
                            EvenMoreFish.userReports.put(event.getPlayer().getUniqueId(), report);
                        }
                    } catch (SQLException exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Could not fetch user reports for: " + event.getPlayer().getUniqueId());
                    }
                }
            }
        }.runTaskAsynchronously(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class));
    }

    // Removes the player from the bar list if they leave the server
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        if (Competition.isActive()) {
            EvenMoreFish.active.getStatusBar().removePlayer(event.getPlayer());
        }

        if (EvenMoreFish.mainConfig.isDatabaseOnline()) {

            new BukkitRunnable() {

                @Override
                public void run() {
                    try {
                        if (!EvenMoreFish.databaseV3.hasUser(event.getPlayer().getUniqueId(), Table.EMF_USERS, true)) {
                            EvenMoreFish.databaseV3.createUser(event.getPlayer().getUniqueId());
                        }
                    } catch (SQLException | InvalidTableException exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Fatal error when running database checks for " + event.getPlayer().getName() + ", deleting data in primary storage.");
                        exception.printStackTrace();
                        return;
                    }

                    if (EvenMoreFish.fishReports.containsKey(event.getPlayer().getUniqueId())) {
                        try {
                            EvenMoreFish.databaseV3.writeFishReports(event.getPlayer().getUniqueId(), EvenMoreFish.fishReports.get(event.getPlayer().getUniqueId()));
                        } catch (SQLException exception) {
                            EvenMoreFish.logger.log(Level.SEVERE, "Fatal error whilst writing " + event.getPlayer().getName() + "'s data to the database.");
                        }
                    }

                    try {
                        EvenMoreFish.databaseV3.writeUserReport(event.getPlayer().getUniqueId(), EvenMoreFish.userReports.get(event.getPlayer().getUniqueId()));
                    } catch (SQLException exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Fatal error writing " + event.getPlayer().getName() + "'s data to the SQL database.");
                        exception.printStackTrace();
                    }

                    EvenMoreFish.fishReports.remove(event.getPlayer().getUniqueId());
                }
            }.runTaskAsynchronously(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class));

        }
    }
}
