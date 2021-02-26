package com.oheers.fish.database;

import org.bukkit.entity.Player;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;

public class Database {

    private static Connection connection;
    private static PreparedStatement prep;
    private static ResultSet rs;

    final static String url = "jdbc:sqlite:plugins/EvenMoreFish/database.db";

    public static void getConnection() throws SQLException {

        // creates a connection to the database
        connection = DriverManager.getConnection(url);

    }

    private static void closeConnections() throws SQLException {
        // memory stuff - closes all the database wotsits
        connection.close();
        if (prep != null) prep.close();
        if (rs != null) rs.close();
    }

    // does the almighty "Fish" database exist
    public static boolean dbExists() throws SQLException {
        ArrayList<String> databases = new ArrayList<>();
        getConnection();

        // gets connection metadata
        DatabaseMetaData dbMD = connection.getMetaData();
        ResultSet tables = dbMD.getTables(null, null, "Fish", null);

        closeConnections();

        // if there's a tables.next() it returns true, if not, it returns false
        return tables.next();
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

        // Creates a table with FISH as the primary key
        prep = connection.prepareStatement(sql);
        prep.execute();

        closeConnections();
    }

    public static boolean hasFish(String name) throws SQLException {
        getConnection();

        String sql = "SELECT FISH FROM Fish;";

        prep = connection.prepareStatement(sql);
        rs = prep.executeQuery();

        // will return false if there's no fish with the given parameter

        while (rs.next()) {
            if (rs.getString(1).equals(name)) {
                closeConnections();
                return true;
            }
        }

        closeConnections();
        return false;
    }

    public static void add(String fish, Player fisher, Float length) throws SQLException {
        getConnection();

        String sql = "INSERT INTO Fish (FISH, firstFisher, totalCaught, largestFish, largestFishCatcher) VALUES (?,?,?,?,?);";

        // rounds it so it's all nice looking for when it goes into the database
        double lengthDouble = Math.round(length*10.0) / 10.0;

        // starts a field for the new fish that's been fished for the first time
        prep = connection.prepareStatement(sql);
        prep.setString(1, fish);
        prep.setString(2, fisher.getUniqueId().toString());
        prep.setInt(3, 1);
        prep.setDouble(4, lengthDouble);
        prep.setString(5, fisher.getUniqueId().toString());
        prep.execute();

        closeConnections();

    }

    // a fish has been caught, the total catches needs incrementing
    public static void fishIncrease(String fishName) throws SQLException {
        getConnection();

        String sql = "UPDATE Fish SET totalCaught = totalCaught + 1 WHERE FISH = ?;";

        prep = connection.prepareStatement(sql);
        prep.setString(1, fishName);
        prep.execute();

        closeConnections();
    }

    // a player has just fished a fish that's longer than the current #1
    public static void newTopSpot(Player player, String fishName, Float length) throws SQLException {
        getConnection();

        String sql = "UPDATE Fish SET largestFish = ?, largestFishCatcher=? WHERE FISH = ?;";

        // rounds it so it's all nice looking for when it goes into the database
        double lengthDouble = Math.round(length*10.0) / 10.0;

        prep = connection.prepareStatement(sql);
        prep.setDouble(1, lengthDouble);
        prep.setString(2, player.getUniqueId().toString());
        prep.setString(3, fishName);
        prep.execute();

        closeConnections();
    }

    public static float getTopLength(String fishName) throws SQLException {
        getConnection();

        String sql = "SELECT largestFish FROM Fish WHERE FISH = ?;";

        prep = connection.prepareStatement(sql);
        prep.setString(1, fishName);

        rs = prep.executeQuery();

        if (rs.next()) {
            float returnable = rs.getFloat(1);
            closeConnections();
            return returnable;
        }

        closeConnections();
        return Float.MAX_VALUE;
    }
}
