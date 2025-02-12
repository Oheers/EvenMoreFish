
package com.oheers.fish.gui.guis.journal;

import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private Connection connection;

    public void connect() {
        try {
            // Ensure the plugins/EmfCodex directory exists
            File dataFolder = new File("plugins/EmfCodex");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:plugins/EmfCodex/fishdata.db");
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS fish_data (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "player_uuid TEXT," +
                        "fish_name TEXT," +
                        "rarity TEXT," +
                        "rarity_colour TEXT," +
                        "times_caught INTEGER," +
                        "largest_size REAL," +
                        "shortest_size REAL," +
                        "discover_date TEXT," +
                        "discoverer TEXT," +
                        "server_best_size REAL," +
                        "server_shortest_size REAL," +
                        "server_caught INTEGER," +
                        "UNIQUE(player_uuid, fish_name)" +
                        ");");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<List<FishData>> getFishData(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            List<FishData> fishDataList = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM fish_data WHERE player_uuid = ?")) {
                statement.setString(1, playerUUID.toString());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    FishData fishData = new FishData(
                            resultSet.getString("fish_name"),
                            resultSet.getString("rarity"),
                            resultSet.getString("rarity_colour"),
                            resultSet.getInt("times_caught"),
                            resultSet.getDouble("largest_size"),
                            resultSet.getDouble("shortest_size"),
                            resultSet.getString("discover_date"),
                            resultSet.getString("discoverer"),
                            resultSet.getDouble("server_best_size"),
                            resultSet.getDouble("server_shortest_size"),
                            resultSet.getInt("server_caught")
                    );
                    fishDataList.add(fishData);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return fishDataList;
        });
    }

    public CompletableFuture<Void> updateFishData(UUID playerUUID, String fishName, String rarity, String rarityColour, double size, String discoverer, double serverBestSize, double serverShortestSize, int serverCaught) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO fish_data (player_uuid, fish_name, rarity, rarity_colour, times_caught, largest_size, shortest_size, discover_date, discoverer, server_best_size, server_shortest_size, server_caught) " +
                    "VALUES (?, ?, ?, ?, 1, ?, ?, datetime('now'), ?, ?, ?, ?) " +
                    "ON CONFLICT(player_uuid, fish_name) DO UPDATE SET " +
                    "rarity = excluded.rarity, " +
                    "rarity_colour = excluded.rarity_colour, " +
                    "times_caught = times_caught + 1, " +
                    "largest_size = CASE WHEN largest_size < excluded.largest_size THEN excluded.largest_size ELSE largest_size END, " +
                    "shortest_size = CASE WHEN shortest_size > excluded.shortest_size THEN excluded.shortest_size ELSE shortest_size END, " +
                    "server_best_size = excluded.server_best_size, " +
                    "server_shortest_size = excluded.server_shortest_size, " +
                    "server_caught = excluded.server_caught";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, fishName);
                statement.setString(3, rarity);
                statement.setString(4, rarityColour);
                statement.setDouble(5, size);
                statement.setDouble(6, size);
                statement.setString(7, discoverer);
                statement.setDouble(8, serverBestSize);
                statement.setDouble(9, serverShortestSize);
                statement.setInt(10, serverCaught);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<FishData> getFishData(UUID playerUUID, String fishName) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM fish_data WHERE player_uuid = ? AND fish_name = ?")) {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, fishName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return new FishData(
                            resultSet.getString("fish_name"),
                            resultSet.getString("rarity"),
                            resultSet.getString("rarity_colour"),
                            resultSet.getInt("times_caught"),
                            resultSet.getDouble("largest_size"),
                            resultSet.getDouble("shortest_size"),
                            resultSet.getString("discover_date"),
                            resultSet.getString("discoverer"),
                            resultSet.getDouble("server_best_size"),
                            resultSet.getDouble("server_shortest_size"),
                            resultSet.getInt("server_caught")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> hasCaughtFishOfRarity(UUID playerUUID, String rarity) {
        return getFishData(playerUUID).thenApply(fishDataList -> {
            for (FishData fishData : fishDataList) {
                if (fishData.getRarity().equalsIgnoreCase(rarity)) {
                    return true;
                }
            }
            return false;
        });
    }

    public CompletableFuture<Double> getServerBestSize(String fishName) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT MAX(largest_size) AS server_best_size FROM fish_data WHERE fish_name = ?")) {
                statement.setString(1, fishName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getDouble("server_best_size");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0.0;
        });
    }

    public CompletableFuture<Double> getServerShortestSize(String fishName) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT MIN(shortest_size) AS server_shortest_size FROM fish_data WHERE fish_name = ?")) {
                statement.setString(1, fishName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getDouble("server_shortest_size");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0.0;
        });
    }

    public CompletableFuture<Integer> getServerCaughtCount(String fishName) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT MAX(times_caught) AS server_caught FROM fish_data WHERE fish_name = ?")) {
                statement.setString(1, fishName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("server_caught");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public CompletableFuture<String> getBestPlayer(String fishName) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT player_uuid FROM fish_data WHERE fish_name = ? ORDER BY largest_size DESC LIMIT 1")) {
                statement.setString(1, fishName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    String playerUUID = resultSet.getString("player_uuid");
                    return Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<String> getMostCaughtPlayer(String fishName) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT player_uuid FROM fish_data WHERE fish_name = ? ORDER BY times_caught DESC LIMIT 1")) {
                statement.setString(1, fishName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    String playerUUID = resultSet.getString("player_uuid");
                    return Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<String> getShortestPlayer(String fishName) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT player_uuid FROM fish_data WHERE fish_name = ? ORDER BY shortest_size ASC LIMIT 1")) {
                statement.setString(1, fishName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    String playerUUID = resultSet.getString("player_uuid");
                    return Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<String> getFirstDiscoverer(String fishName) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT discoverer FROM fish_data WHERE fish_name = ? ORDER BY discover_date ASC LIMIT 1")) {
                statement.setString(1, fishName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("discoverer");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public List<FishData> getFishDataSync(UUID playerId) {
        return fetchFishDataFromDatabase(playerId);
    }

    private List<FishData> fetchFishDataFromDatabase(UUID playerId) {
        List<FishData> fishDataList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM fish_data WHERE player_uuid = ?")) {
            statement.setString(1, playerId.toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                FishData fishData = new FishData(
                        resultSet.getString("fish_name"),
                        resultSet.getString("rarity"),
                        resultSet.getString("rarity_colour"),
                        resultSet.getInt("times_caught"),
                        resultSet.getDouble("largest_size"),
                        resultSet.getDouble("shortest_size"),
                        resultSet.getString("discover_date"),
                        resultSet.getString("discoverer"),
                        resultSet.getDouble("server_best_size"),
                        resultSet.getDouble("server_shortest_size"),
                        resultSet.getInt("server_caught")
                );
                fishDataList.add(fishData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fishDataList;
    }
}