package com.oheers.fish.database;

import com.oheers.fish.EvenMoreFish;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.logging.Level;

public class DatabaseV3 {

	private final String URL;
	private boolean isMySQL = EvenMoreFish.mainConfig.isMysql();

	private final String username = EvenMoreFish.mainConfig.getUsername();
	private final String password = EvenMoreFish.mainConfig.getPassword();
	private final String address = EvenMoreFish.mainConfig.getAddress();
	private final String database = EvenMoreFish.mainConfig.getDatabase();

	private Connection connection;

	/**
	 * Creates a connection to the database to send/receive data. This must be closed with the #closeConnection() method
	 * once the connection is no longer needed.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	private void getConnection() throws SQLException {
		if (connection == null) {
			if (isMySQL) connection = DriverManager.getConnection(URL, username, password);
			else connection = DriverManager.getConnection(URL);
		}
	}

	/**
	 * Closes the connection to the database to save memory and prevent memory leaks.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	private void closeConnection() throws SQLException {
		if (connection != null) connection.close();
	}

	/**
	 * This gets the database metadata and queries whether the table exists using the #getTables method. The connection
	 * passed through must be open.
	 *
	 * @param tableID The name of the table to be queried.
	 * @param activeConnection The currently open connection.
	 * @return If the table exists, true will be returned. If it doesn't, false is returned.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	private boolean queryTableExistence(@NotNull final String tableID, @NotNull final Connection activeConnection) throws SQLException {
		DatabaseMetaData dbMetaData = activeConnection.getMetaData();
		try (ResultSet tables = dbMetaData.getTables(null, null, tableID, null)) {
			return tables.next();
		}
	}

	/**
	 * This creates all tables that don't exist within the database. If the table does exist however it is skipped, to prevent
	 * duplicate tables (if that's even possible)
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	public void createTables() throws SQLException {
		getConnection();

		for (Table table : Table.values()) {
			if (!queryTableExistence(table.getTableID(), this.connection)) {
				if (table.getCreationCode() != null) sendStatement(table.getCreationCode(), this.connection);
			}
		}

		closeConnection();
	}
	/**
	 * Gets the URL for the database, if the config states to use MySQL then the url will point to where it needs to
	 * requiring a username & password are provided. If not it just uses local .db file anyway.
	 *
	 * @return The URL needed to access the database.
	 */
	private String fetchURL() {
		if (isMySQL) {
			if (username != null &&	password != null && address != null && database != null) {
				return "jdbc:mysql://" + address + "/" + database;
			} else {
				EvenMoreFish.logger.log(Level.SEVERE, "MySQL credentials do not exist, using local database file.");
				isMySQL = false;
			}
		}
		return "jdbc:sqlite:plugins/EvenMoreFish/database.db";
	}

	/**
	 * Creates a prepared statement that is sent to the database to be executed. Examples could be to write data or
	 * create a new table. Data cannot be fetched using this method.
	 *
	 * @param sqlCode The SQL code to go into the prepared statement.
	 * @param activeConnection The currently open connection.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	private void sendStatement(@NotNull final String sqlCode, @NotNull final Connection activeConnection) throws SQLException {
		try (PreparedStatement prep = activeConnection.prepareStatement(sqlCode)) {
			prep.execute();
		}
	}

	/**
	 * This is a reference to all database activity within the EMF plugin. It improves on the previous DatabaseV2 in that
	 * when the config states that data must be stored in MySQL it won't then go and store the data in flat-file format.
	 * The database structure contains three tables, emf_users, emf_fish and emf_competitions. The emf_users table is used
	 * to store the data that makes up UserReports. The emf_fish table stores data to make up a FishReport and you guessed it,
	 * the emf_competitions stores data for a CompetitionReport.
	 *
	 * In DatabaseV2 there were just fish reports which was all well and good, but they were always stored in flat-file, and
	 * it was nearly impossible to store accurate data on a user. The CompetitionReports store each competition with its
	 * own "id", this is unique to every single competition, even ones with the same name, same time but a different week.
	 * Data such as who won them, which fish won and the total number of participants is stored there.
	 */
	public DatabaseV3() {
		this.URL = fetchURL();
		this.isMySQL = false;

		try {
			createTables();
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Failed to create new tables or check for present tables.");
			exception.printStackTrace();
		}
	}
}
