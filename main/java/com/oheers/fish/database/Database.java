package com.oheers.fish.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.oheers.fish.EvenMoreFish;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Database {

    static final String url = "jdbc:sqlite:plugins/EvenMoreFish/database.db";
    private static Connection connection;

    public static void getConnection() throws SQLException {
        if (connection == null) {
            // creates a connection to the database
            connection = DriverManager.getConnection(url);
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
        try (ResultSet tables = dbMD.getTables(null, null, "Fish", null)) {
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

        String sql = "CREATE TABLE \"Fish\" (\n" +
                "\"FISH\"\tTEXT,\n" +
                "\"firstFisher\" TEXT,\n" +
                "\"totalCaught\" INTEGER,\n" +
                "\"largestFish\" FLOAT,\n" +
                "\"largestFishCatcher\" TEXT,\n" +
                "PRIMARY KEY(\"FISH\")\n" +
                ");";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            // Creates a table with FISH as the primary key
            prep.execute();
        }
    }

    public static void createUserTable() throws SQLException {
        getConnection();

        String sql = "CREATE TABLE \"Users\" (\n" +
				"\t\"uuid\"\tTEXT,\n" +
				"\t\"fish_caught\"\tTEXT,\n" +
				"\t\"competitions_won\"\tINTEGER,\n" +
				"\tPRIMARY KEY(\"uuid\")\n" +
				");";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.execute();
        }
    }

    public static boolean hasFish(String name) throws SQLException {
        getConnection();

        String sql = "SELECT FISH FROM Fish;";

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

    public static void add(String fish, Player fisher, Float length) throws SQLException {
        getConnection();

        String sql = "INSERT INTO Fish (FISH, firstFisher, totalCaught, largestFish, largestFishCatcher) VALUES (?,?,?,?,?);";

        // rounds it so it's all nice looking for when it goes into the database
        double lengthDouble = Math.round(length * 10.0) / 10.0;

        // starts a field for the new fish that's been fished for the first time
        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, fish);
            prep.setString(2, fisher.getUniqueId().toString());
            prep.setInt(3, 1);
            prep.setDouble(4, lengthDouble);
            prep.setString(5, fisher.getUniqueId().toString());
            prep.execute();
        }
    }

    // a fish has been caught, the total catches needs incrementing
    public static void fishIncrease(String fishName) throws SQLException {
        getConnection();

        String sql = "UPDATE Fish SET totalCaught = totalCaught + 1 WHERE FISH = ?;";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, fishName);
            prep.execute();
        }
    }

    // a player has just fished a fish that's longer than the current #1
    public static void newTopSpot(Player player, String fishName, Float length) throws SQLException {
        getConnection();

        String sql = "UPDATE Fish SET largestFish = ?, largestFishCatcher=? WHERE FISH = ?;";

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

        String sql = "SELECT largestFish FROM Fish WHERE FISH = ?;";

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

    public static boolean hasUser(String uuid) {
        String sql = "SELECT uuid FROM Users WHERE uuid = ?;";

        try {
            PreparedStatement prep = connection.prepareStatement(sql);
            prep.setString(1, uuid);
            ResultSet rs = prep.executeQuery();
			if (rs.next()) {
				rs.close();
				return true;
			} else return false;
        } catch (SQLException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, "Could not test for existence of " + uuid + " in the table: Users.");
            exception.printStackTrace();
        }
        return false;
    }

	public static void addUser(String uuid, List<FishReport> reports) {

		try { getConnection(); }
		catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not add " + uuid + " in the table: Users.");
			exception.printStackTrace();
			return;
		}

		String sql = "INSERT INTO Users (uuid, fish_caught, competitions_won) VALUES (?,?,?);";
		Gson gson = new Gson();

		// starts a field for the new fish that's been fished for the first time
		try (PreparedStatement prep = connection.prepareStatement(sql)) {

			prep.setString(1, uuid);
			prep.setString(2, gson.toJson(reports));
			prep.setInt(3, 0);
			prep.execute();

		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not add " + uuid + " in the table: Users.");
			exception.printStackTrace();
		}
	}

    public static List<FishReport> readUserData(String uuid) {

        String sql = "SELECT fish_caught FROM Users WHERE uuid = ?;";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            getConnection();

            prep.setString(1, uuid);
            ResultSet rs = prep.executeQuery();

			Type fishReportList = new TypeToken<ArrayList<FishReport>>(){}.getType();
			List<FishReport> reports = new ArrayList<>();

            while (rs.next()) {
                Gson gson = new Gson();
                reports = gson.fromJson(rs.getString("fish_caught"), fishReportList);
            }
			rs.close();
            return reports;
        } catch (SQLException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, "Could not access " + uuid + "'s fish_caught record from the database. More errors will likely occur.");
            exception.printStackTrace();
            return null;
        }
    }

    public static void writeUserData(String uuid, List<FishReport> reports) {

        String sql = "UPDATE Users SET fish_caught = ? WHERE uuid = ?;";

        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            getConnection();

            Gson gson = new Gson();
            prep.setString(1, gson.toJson(reports));
            prep.setString(2, uuid);
            prep.execute();
        } catch (SQLException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, "Could not write to " + uuid + "'s fish_caught record from the database. More errors will likely occur.");
            exception.printStackTrace();
        }
    }

}
