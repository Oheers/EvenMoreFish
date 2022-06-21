package com.oheers.fish.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.PrefixType;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
		if (connection == null || connection.isClosed()) {
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
	 * Checks for the existence the /data/ directory as a way of detecting whether the plugin is using V2. If the
	 * flat-file folder is present then it's using V2, since the /emf migrate command upgrades the plugin to
	 *
	 * @return If the server is still using the V2 database.
	 */
	public boolean usingVersionV2() {
		return Files.isDirectory(Paths.get(JavaPlugin.getProvidingPlugin(DatabaseV3.class).getDataFolder() + "/data/"));
	}

	/**
	 * This creates all tables that don't exist within the database. If the table does exist however it is skipped, to prevent
	 * duplicate tables (if that's even possible). If the server is using V2 then the tables won't be created to prevent
	 * data duplication. The server should use the /emf migrate command to move over their data to the new V3 engine.
	 *
	 * @param overrideV2Check Whether the plugin should override checking for the /data/ folder - this should only be
	 *                        done when migrating
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	public void createTables(final boolean overrideV2Check) throws SQLException {
		if (!overrideV2Check && usingVersionV2()) {
			EvenMoreFish.logger.log(Level.SEVERE, "Your server is running EMF database V2. To continue using database functionality you need to run /emf migrate.");
			return;
		}

		getConnection();

		for (Table table : Table.values()) {
			if (!queryTableExistence(table.getTableID(), this.connection)) {
				if (table.getCreationCode() != null) sendStatement(table.getCreationCode(), this.connection);
			}
		}

		closeConnection();
	}

	/**
	 * Converts a V2 database system to a V3 database system. The server must not crash during this process as this may
	 * lead to data loss, but honestly I'm not 100% sure on that one. Data is read from the /data/ folder and is
	 * inserted into the new database system then the /data/ folder is renamed to /data-old/.
	 *
	 * @param initiator The person who started the migration.
	 */
	public void migrate(CommandSender initiator) {
		if (!usingVersionV2()) {
			Message msg = new Message("EvenMoreFish is already using the latest V3 database engine.");
			msg.usePrefix(PrefixType.ERROR);
			msg.broadcast(initiator, true, false);
			return;
		}

		EvenMoreFish.logger.log(Level.INFO, initiator.getName() + " has begun the migration to EMF database V3 from V2.");
		Message msg = new Message("Beginning conversion to V3 database engine.");
		msg.usePrefix(PrefixType.ADMIN);
		msg.broadcast(initiator, true, false);

		try {
			translateFishDataV2();
			createTables(true);

			File dataFolder = new File(JavaPlugin.getProvidingPlugin(DatabaseV3.class).getDataFolder() + "/data/");
			for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
				Type fishReportList = new TypeToken<List<FishReport>>(){}.getType();

				Gson gson = new Gson();
				List<FishReport> reports = gson.fromJson(new FileReader(file), fishReportList);
				UUID playerUUID = UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf(".")));

				createUser(playerUUID);
				translateFishReportsV2(playerUUID, reports);

				Message migratedMSG = new Message("Migrated: " + playerUUID);
				migratedMSG.usePrefix(PrefixType.ERROR);
				migratedMSG.broadcast(initiator, true, false);

				file.delete();
			}
		} catch (NullPointerException | SQLException | FileNotFoundException exception) {
			exception.printStackTrace();
			Message message = new Message("Fatal error whilst upgrading to V3 database engine.");
			message.usePrefix(PrefixType.ERROR);
			message.broadcast(initiator, true, false);
		}
	}

	/**
	 * This causes a renaming of the table "Fish2" to "emf_fish", no data internally changes, but it's good to have a clean
	 * format for all the tables and to have a more descriptive name for this stuff.
	 */
	private void translateFishDataV2() throws SQLException {
		getConnection();
		if (queryTableExistence(Table.EMF_FISH.getTableID(), this.connection)) {
			try {
				return;
			} finally {
				closeConnection();
			}
		}
		sendStatement("ALTER TABLE Fish2 RENAME TO " + Table.EMF_FISH.getTableID() + ";", this.connection);
		closeConnection();
	}

	/**
	 * Loops through each fish report passed through and sets all the default values for the user in the database. Note
	 * that the user must already have a field within the database. All data regarding competitions and the total size does
	 * not exist within the V2 and V1 recording system so are set to 0 by default, as this lost data cannot be recovered.
	 * Similarly, the latest fish cannot be retrieved so the "None" fish is left, to reference there is no value here yet.
	 *
	 * This should only be used during the V1/2 -> V3 migration process.
	 *
	 * @param uuid The user
	 * @param reports The V2 fish reports associated with the user.
	 */
	private void translateFishReportsV2(final UUID uuid, final List<FishReport> reports) {
		String firstFishID = "";
		long epochFirst = Long.MAX_VALUE;
		String largestFishID = "";
		float largestSize = 0f;

		int totalFish = 0;

		try {
			getConnection();
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Fatal error whilst upgrading to V3 database engine.");
		}

		for (FishReport report : reports) {
			if (report.getTimeEpoch() < epochFirst) {
				epochFirst = report.getTimeEpoch();
				firstFishID = report.getRarity() + ":" + report.getName();
			}
			if (report.getLargestLength() > largestSize) {
				largestSize = report.getLargestLength();
				largestFishID = report.getRarity() + ":" + report.getName();
			}

			totalFish += report.getNumCaught();



			// starts a field for the new fish for the user that's been fished for the first time
			try {
				// "Statement is not executing" when using setString yada yada... This seems to work though.
				String emfFishLogSQL = "INSERT INTO emf_fish_log (id, rarity, fish, quantity, first_catch_time, largest_length) VALUES (" +
						getUserID(uuid) + ", \"" +
						report.getRarity() + "\", \"" +
						report.getName() + "\", " +
						report.getNumCaught() + ", " +
						report.getTimeEpoch() + ", " +
						report.getLargestLength() + ");";
				PreparedStatement prep = connection.prepareStatement(emfFishLogSQL);

				prep.execute();
			} catch (SQLException exception) {
				EvenMoreFish.logger.log(Level.SEVERE, "Could not add " + uuid + " in the table: Users.");
				exception.printStackTrace();
			}
		}

		String emfUsersSQL = "UPDATE emf_users SET first_fish = ?, largest_fish = ?, num_fish_caught = ? WHERE uuid = ?;";

		// starts a field for the new fish that's been fished for the first time
		try {
			PreparedStatement prep = connection.prepareStatement(emfUsersSQL);
			prep.setString(1, firstFishID);
			prep.setString(2, largestFishID);
			prep.setInt(3, totalFish);
			prep.setString(4, uuid.toString());

			prep.execute();
			closeConnection();
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not add " + uuid + " in the table: emf_users.");
			exception.printStackTrace();
		}
	}

	/**
	 * Returns the user's ID from the emf_users table. If there is no user, the value 0 will be returned to indicate
	 * that the user is not yet present in the table. This ID can be used for the emf_fish_log table which is used
	 * to store the user's fish log.
	 *
	 * @param uuid The UUID of the user being queried.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 * @return The ID of the user for the database, 0 if there is no user present matching the UUID.
	 */
	public int getUserID(@NotNull final UUID uuid) throws SQLException {
		getConnection();

		PreparedStatement statement = connection.prepareStatement("SELECT id FROM emf_users WHERE uuid = ?;");
		statement.setString(1, uuid.toString());
		ResultSet resultSet = statement.executeQuery();

		return resultSet.getInt("id");
	}

	/**
	 * Creates an empty user field for the user in the database. Their ID is created by an auto increment and all first,
	 * last and largest fish are set to "None"
	 *
	 * @param uuid The user field to be created.
	 */
	private void createUser(UUID uuid) {
		String sql = "INSERT INTO emf_users (uuid, first_fish, last_fish, largest_fish, num_fish_caught, total_fish_length," +
				"competitions_won, competitions_joined) VALUES (?, \"None\",\"None\",\"None\", 0, 0, 0, 0);";

		// starts a field for the new fish that's been fished for the first time
		try {
			getConnection();
			PreparedStatement prep = connection.prepareStatement(sql);
			prep.setString(1, uuid.toString());
			prep.execute();
			closeConnection();
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not add " + uuid + " in the table: Users.");
			exception.printStackTrace();
		}
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
			createTables(false);
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Failed to create new tables or check for present tables.");
			exception.printStackTrace();
		}
	}
}
