package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.database.DataManager;
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
        if (EvenMoreFish.mainConfig.isDatabaseOnline() && EvenMoreFish.mainConfig.doingExperimentalFeatures()) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    try {
                        EvenMoreFish.databaseV3.getConnection();
                        List<FishReport> fishReports = new ArrayList<>();

                        try {
                            if (EvenMoreFish.databaseV3.hasUser(userUUID, Table.EMF_FISH_LOG)) {
                                fishReports = EvenMoreFish.databaseV3.getFishReports(userUUID);
                            } else {
                                fishReports = new ArrayList<>();
                                if (EvenMoreFish.mainConfig.doDBVerbose())
                                    EvenMoreFish.logger.log(Level.INFO, userName + " has joined for the first time, creating new data handle for them.");
                            }
                        } catch (SQLException | InvalidTableException exception) {
                            EvenMoreFish.logger.log(Level.SEVERE, "Failed to check database existence of user " + userUUID);
                            exception.printStackTrace();
                        }

                        UserReport userReport = null;
                        try {
                            userReport = EvenMoreFish.databaseV3.readUserReport(userUUID);
                            if (userReport == null) {
                                EvenMoreFish.databaseV3.createUser(userUUID);
                                userReport = EvenMoreFish.databaseV3.readUserReport(userUUID);
                            }
                        } catch (SQLException exception) {
                            EvenMoreFish.logger.log(Level.SEVERE, "Could not fetch user reports for: " + userUUID);
                        }

                        if (fishReports != null && userReport != null) {
                            DataManager.getInstance().cacheUser(userUUID, userReport, fishReports);
                        } else {
                            EvenMoreFish.logger.log(Level.SEVERE, "Null value when fetching data for user (" + userName + "),\n" +
                                    "UserReport: " + (userReport == null) +
                                    ",\nFishReports: " + (fishReports != null && fishReports.size() > 0));
                        }

                        EvenMoreFish.databaseV3.closeConnection();
                    } catch (SQLException exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Failed SQL operations whilst fetching user data for " + userName + ". Try restarting or contacting support.");
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

        if (EvenMoreFish.mainConfig.isDatabaseOnline() && EvenMoreFish.mainConfig.doingExperimentalFeatures()) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    try {
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

                        List<FishReport> fishReports = DataManager.getInstance().getFishReportsIfExists(userUUID);
                        if (fishReports != null) {
                            try {
                                EvenMoreFish.databaseV3.writeFishReports(userUUID, fishReports);
                            } catch (SQLException exception) {
                                EvenMoreFish.logger.log(Level.SEVERE, "Fatal error whilst writing " + event.getPlayer().getName() + "'s data to the database.");
                            }
                        }

                        UserReport userReport = DataManager.getInstance().getUserReportIfExists(userUUID);
                        if (userReport != null) {
                            try {
                                EvenMoreFish.databaseV3.writeUserReport(userUUID, userReport);
                            } catch (SQLException exception) {
                                EvenMoreFish.logger.log(Level.SEVERE, "Fatal error writing " + event.getPlayer().getName() + "'s data to the SQL database.");
                                exception.printStackTrace();
                            }
                        }

                        DataManager.getInstance().uncacheUser(userUUID);
                        EvenMoreFish.databaseV3.closeConnection();

                    } catch (SQLException exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Failed SQL operations whilst writing data for user " + event.getPlayer().getName() + ". Try restarting or contacting support.");
                        exception.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(JavaPlugin.getProvidingPlugin(JoinChecker.class));
        }
    }
}
