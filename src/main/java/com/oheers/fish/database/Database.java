package com.oheers.fish.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

public class Database {

    static String url; // = "jdbc:sqlite:plugins/EvenMoreFish/database.db";
    static boolean isMysql = EvenMoreFish.mainConfig.isMysql();
    private static Connection connection;
    private final static String username = EvenMoreFish.mainConfig.getUsername();
    private final static String password = EvenMoreFish.mainConfig.getPassword();
    private static FileReader reader;

    public static void getUrl() {
        if (isMysql) {
            String address = EvenMoreFish.mainConfig.getAddress();
            String database = EvenMoreFish.mainConfig.getDatabase();
            if (address != null && username != null && password != null && database != null) {
                url = "jdbc:mysql://" + address + "/" + database;
                return;
            } else {
                EvenMoreFish.logger.log(Level.SEVERE, "MySQL credentials do not exist, using alternative database system.");
                isMysql = false;
            }
        }

        url = "jdbc:sqlite:plugins/EvenMoreFish/database.db";

    }

    public static void getConnection() throws SQLException {
        if (connection == null) {
            // creates a connection to the database
            if (isMysql) connection = DriverManager.getConnection(url, username, password);
            else connection = DriverManager.getConnection(url);
        }
    }

    public static void closeConnections() throws SQLException {
        // memory stuff - closes all the database wotsits
        if (connection != null) {
            connection.close();
        }
    }

    // does the almighty "Fish" database exist
    public static boolean fishTableExists() throws SQLException {
        getConnection();

        // gets connection metadata
        DatabaseMetaData dbMD = connection.getMetaData();
        try (ResultSet tables = dbMD.getTables(null, null, "Fish2", null)) {
            // if there's a tables.next() it returns true, if not, it returns false
            return tables.next();
        }
    }

    // does the almighty "Users" database exist
    public static boolean userTableExists() throws SQLException {
        getConnection();

        // gets connection metadata
        DatabaseMetaData dbMD = connection.getMetaData();
        try (ResultSet tables = dbMD.getTables(null, null, "Users", null)) {
            // if there's a tables.next() it returns true, if not, it returns false
            return tables.next();
        }
    }

    public static void createDatabase() throws SQLException {
        getConnection();

        String sql = "CREATE TABLE Fish2 (\n" +
                "    fish_name VARCHAR(100) NOT NULL,\n" +
                "    fish_rarity VARCHAR(100) NOT NULL,\n" +
                "    first_fisher VARCHAR(36) NOT NULL, \n" +
                "    total_caught INTEGER NOT NULL,\n" +
                "    largest_fish REAL NOT NULL,\n" +
                "    largest_fisher VARCHAR(36) NOT NULL,\n" +
                "    first_catch_time LONGBLOB NOT NULL\n" +
                ");";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            // Creates a table with FISH as the primary key
            prep.execute();
        }
    }

    public static void createUserTable() throws SQLException {
        getConnection();

        String sql = "CREATE TABLE Users(\n" +
                "    uuid VARCHAR(36),\n" +
                "    competitions_won INTEGER,\n" +
                "    fish_sold INTEGER,\n" +
                "    PRIMARY KEY(UUID)\n" +
                ");";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.execute();
        }
    }

    public static boolean hasFish(String name) throws SQLException {
        getConnection();

        String sql = "SELECT fish_name FROM Fish2;";

        try (
                PreparedStatement prep = connection.prepareStatement(sql);
                ResultSet rs = prep.executeQuery()
        ) {
            while (rs.next()) {
                if (rs.getString(1).equals(name)) {
                    return true;
                }
            }
            // will return false if there's no fish with the given parameter
            return false;
        }
    }

    public static void add(Fish fish, Player fisher) throws SQLException {
        getConnection();

        String sql = "INSERT INTO Fish2 (fish_name, fish_rarity, first_fisher, total_caught, largest_fish, largest_fisher, first_catch_time) VALUES (?,?,?,?,?,?,?);";

        // starts a field for the new fish that's been fished for the first time
        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, fish.getName());
            prep.setString(2, fish.getRarity().getValue());
            prep.setString(3, fisher.getUniqueId().toString());
            prep.setDouble(4, 1);
            prep.setFloat(5, fish.getLength());
            prep.setString(6, fisher.getUniqueId().toString());
            prep.setLong(7, Instant.now().getEpochSecond());
            prep.execute();
        }
    }

    // a fish has been caught, the total catches needs incrementing
    public static void fishIncrease(String fishName) throws SQLException {
        getConnection();

        String sql = "UPDATE Fish2 SET total_caught = total_caught + 1 WHERE fish_name = ?;";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, fishName);
            prep.execute();
        }
    }

    // a player has just fished a fish that's longer than the current #1
    public static void newTopSpot(Player player, String fishName, Float length) throws SQLException {
        getConnection();

        String sql = "UPDATE Fish2 SET largest_fish = ?, largest_fisher=? WHERE fish_name = ?;";

        // rounds it so it's all nice looking for when it goes into the database
        double lengthDouble = Math.round(length * 10.0) / 10.0;

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setDouble(1, lengthDouble);
            prep.setString(2, player.getUniqueId().toString());
            prep.setString(3, fishName);
            prep.execute();
        }
    }

    public static float getTopLength(String fishName) throws SQLException {
        getConnection();

        String sql = "SELECT largest_fish FROM Fish2 WHERE fish_name = ?;";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, fishName);
            float returnable = Float.MAX_VALUE;
            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                returnable = rs.getFloat(1);
            }
            rs.close();
            return returnable;
        }
    }

    /**
     * Checks the /data/ directory for an existence of a file representing the user.
     *
     * @param uuid The user being queried.
     * @return If the file exists or not.
     */
    public static boolean hasFlatFile(String uuid) {
        return new File(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class).getDataFolder() + "/data/" + uuid + ".json").exists();
    }

    public static boolean hasUser(String uuid) {
        String sql = "SELECT uuid FROM Users WHERE uuid = ?;";

        try {
            getConnection();
            PreparedStatement prep = connection.prepareStatement(sql);
            prep.setString(1, uuid);
            ResultSet rs = prep.executeQuery();
			if (rs.next()) {
				rs.close();
                closeConnections();
				return true;
			} else {
                closeConnections();
                return false;
            }
        } catch (SQLException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, "Could not test for existence of " + uuid + " in the table: Users.");
            exception.printStackTrace();
        }
        return false;
    }

	public static void addUser(String uuid) {

		try { getConnection(); }
		catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not add " + uuid + " in the table: Users.");
			exception.printStackTrace();
			return;
		}

		String sql = "INSERT INTO Users (uuid, competitions_won, fish_sold) VALUES (?,?,?);";

		// starts a field for the new fish that's been fished for the first time
		try (PreparedStatement prep = connection.prepareStatement(sql)) {

			prep.setString(1, uuid);
			prep.setInt(2, 0);
			prep.setInt(3, 0);
			prep.execute();

		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not add " + uuid + " in the table: Users.");
			exception.printStackTrace();
		}
	}

    /**
     * Adds the user data found in flat-file format from /data/ directory and returns it. Checks must first be done to
     * ensure the file exists.
     *
     * @param uuid The UUID of the user being checked.
     * @throws FileNotFoundException If the user file does not exist.
     * @return The list of fish reports fetched from the user.
     */
    public static List<FishReport> readUserData(String uuid) throws FileNotFoundException{

        FileReader reader = new FileReader(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class).getDataFolder() + "/data/" + uuid + ".json");
        Type fishReportList = new TypeToken<List<FishReport>>(){}.getType();

        Gson gson = new Gson();

        return gson.fromJson(reader, fishReportList);
    }

    public static void writeUserData(String uuid, List<FishReport> reports) {

        File userFile = new File(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class).getDataFolder() + "/data/" + uuid + ".json");

        try {
            if (!userFile.exists()) {
                if (!userFile.getParentFile().exists()) {
                    if (!userFile.getParentFile().mkdir()) {
                        throw new IOException("Could not create parent data folder when storing data for: " + uuid);
                    }
                }

                if (!userFile.createNewFile()) {
                    throw new IOException("Could not create data file for: " + uuid);
                }
            }

            String json = new GsonBuilder().setPrettyPrinting().create().toJson(reports);

            FileWriter writer = new FileWriter(userFile);
            writer.write(json);
            writer.close();

        } catch (IOException e) {
            EvenMoreFish.logger.log(Level.SEVERE, "Data could not be written to disk for: " + uuid);
            e.printStackTrace();
        }
    }
}
