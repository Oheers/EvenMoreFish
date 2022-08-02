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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class JoinChecker implements Listener {

    /**
     * Reads all the database information for the user specified.
     *
     * @param userUUID The user UUID having their data read.
     * @param userName the in-game username of the user having their data read.
     */
    public void databaseRegistration(UUID userUUID, String userName) {
        if (EvenMoreFish.mainConfig.isDatabaseOnline()) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    try {
                        EvenMoreFish.v3Semaphore.acquire();
                        EvenMoreFish.databaseV3.getConnection();
                        List<FishReport> reports = new ArrayList<>();

                        try {
                            if (EvenMoreFish.databaseV3.hasUser(userUUID, Table.EMF_FISH_LOG)) {
                                reports = EvenMoreFish.databaseV3.getFishReports(userUUID);
                            } else {
                                reports = new ArrayList<>();
                                if (EvenMoreFish.mainConfig.doDBVerbose())
                                    EvenMoreFish.logger.log(Level.INFO, userName + " has joined for the first time, creating new data handle for them.");
                            }
                        } catch (SQLException | InvalidTableException exception) {
                            EvenMoreFish.logger.log(Level.SEVERE, "Failed to check database existence of user " + userUUID);
                            exception.printStackTrace();
                        }

                        if (reports != null) {
                            EvenMoreFish.fishReports.put(userUUID, reports);
                        } else {
                            EvenMoreFish.logger.log(Level.SEVERE, "Fetched a null reports file for: " + userUUID);
                        }

                        try {
                            UserReport report = EvenMoreFish.databaseV3.readUserReport(userUUID);
                            if (report == null) {
                                EvenMoreFish.databaseV3.createUser(userUUID);
                                report = EvenMoreFish.databaseV3.readUserReport(userUUID);
                                if (report == null) {
                                    EvenMoreFish.logger.log(Level.SEVERE, "Failed to create new empty user report for " + userName);
                                } else {
                                    EvenMoreFish.userReports.put(userUUID, report);
                                }
                            } else {
                                EvenMoreFish.userReports.put(userUUID, report);
                            }
                        } catch (SQLException exception) {
                            EvenMoreFish.logger.log(Level.SEVERE, "Could not fetch user reports for: " + userUUID);
                        }
                        EvenMoreFish.databaseV3.closeConnection();
                        EvenMoreFish.v3Semaphore.release();
                    } catch (SQLException exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Failed SQL operations whilst fetching user data for " + userName + ". Try restarting or contacting support.");
                        exception.printStackTrace();
                    } catch (InterruptedException exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Severe interruption when fetching user data for " + userName);
                        exception.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(JavaPlugin.getProvidingPlugin(JoinChecker.class));
        }
    }

    // Gives the player the active fishing bar if there's a fishing event cracking off
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Competition.isActive()) {
            EvenMoreFish.active.getStatusBar().addPlayer(event.getPlayer());
            EvenMoreFish.active.getStartMessage().setMessage(ConfigMessage.COMPETITION_JOIN);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EvenMoreFish.getPlugin(EvenMoreFish.class),
                    () -> EvenMoreFish.active.getStartMessage().broadcast(event.getPlayer(), true, true), 20 * 3);
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                databaseRegistration(event.getPlayer().getUniqueId(), event.getPlayer().getName());
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
                        EvenMoreFish.v3Semaphore.acquire();
                        EvenMoreFish.databaseV3.getConnection();
                        UUID userUUID = event.getPlayer().getUniqueId();
                        try {
                            if (!EvenMoreFish.databaseV3.hasUser(userUUID, Table.EMF_USERS)) {
                                EvenMoreFish.databaseV3.createUser(userUUID);
                            }
                        } catch (SQLException | InvalidTableException exception) {
                            EvenMoreFish.logger.log(Level.SEVERE, "Fatal error when running database checks for " + event.getPlayer().getName() + ", deleting data in primary storage.");
                            exception.printStackTrace();
                            return;
                        }

                        if (EvenMoreFish.fishReports.containsKey(userUUID)) {
                            try {
                                EvenMoreFish.databaseV3.writeFishReports(userUUID, EvenMoreFish.fishReports.get(userUUID));
                            } catch (SQLException exception) {
                                EvenMoreFish.logger.log(Level.SEVERE, "Fatal error whilst writing " + event.getPlayer().getName() + "'s data to the database.");
                            }
                        }

                        try {
                            EvenMoreFish.databaseV3.writeUserReport(userUUID, EvenMoreFish.userReports.get(userUUID));
                        } catch (SQLException exception) {
                            EvenMoreFish.logger.log(Level.SEVERE, "Fatal error writing " + event.getPlayer().getName() + "'s data to the SQL database.");
                            exception.printStackTrace();
                        }

                        EvenMoreFish.fishReports.remove(userUUID);
                        EvenMoreFish.userReports.remove(userUUID);
                        EvenMoreFish.databaseV3.closeConnection();
                        EvenMoreFish.v3Semaphore.release();
                    } catch (SQLException exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Failed SQL operations whilst writing data for user " + event.getPlayer().getName() + ". Try restarting or contacting support.");
                        exception.printStackTrace();
                    } catch (InterruptedException exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Severe interruption when writing data for user " + event.getPlayer().getName());
                        exception.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(JavaPlugin.getProvidingPlugin(JoinChecker.class));
        }
    }
}
