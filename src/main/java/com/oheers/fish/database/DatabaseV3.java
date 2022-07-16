package com.oheers.fish.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.Leaderboard;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.PrefixType;
import com.oheers.fish.exceptions.InvalidTableException;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

public class DatabaseV3 {

	private final String URL;
	private boolean isMySQL;

	private final String username = EvenMoreFish.mainConfig.getUsername();
	private final String password = EvenMoreFish.mainConfig.getPassword();
	private final String address = EvenMoreFish.mainConfig.getAddress();
	private final String database = EvenMoreFish.mainConfig.getDatabase();

	private Connection connection;

	private boolean usingV2 = Files.isDirectory(Paths.get(JavaPlugin.getProvidingPlugin(DatabaseV3.class).getDataFolder() + "/data/"));

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
		return this.usingV2;
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
			if (queryTableExistence(table.getTableID(), this.connection)) continue;
			if (table.getCreationCode() == null) continue;
			if (isMySQL && !table.isMySQLCompatible) continue;

			sendStatement(table.getCreationCode(), this.connection);
		}

		closeConnection();
	}

	/**
	 * Adds the fish data to the live fish reports list, or changes the existing matching fish report. The plugin will
	 * also account for a new top fish record and set any other data to a new fishing report for example the epoch
	 * time.
	 *
	 * @param uuid The UUID of the user.
	 * @param fish The fish object.
	 */
	public void handleFishCatch(@NotNull final UUID uuid, @NotNull final Fish fish) {
		if (EvenMoreFish.fishReports.containsKey(uuid)) {
			for (FishReport report : EvenMoreFish.fishReports.get(uuid)) {
				if (report.getRarity().equals(fish.getRarity().getValue()) && report.getName().equals(fish.getName())) {
					report.addFish(fish);
					return;
				}
			}
			EvenMoreFish.fishReports.get(uuid).add(
					new FishReport(
							fish.getRarity().getValue(),
							fish.getName(),
							fish.getLength(),
							1
					)
			);
		} else {
			List<FishReport> reports = new ArrayList<>(Collections.singletonList(
				new FishReport(
					fish.getRarity().getValue(),
					fish.getName(),
					fish.getLength(),
					1
				)
			));
			EvenMoreFish.fishReports.put(uuid, reports);
		}

		if (EvenMoreFish.userReports.containsKey(uuid)) {
			UserReport report = EvenMoreFish.userReports.get(uuid);
			String fishID = fish.getRarity().getValue() + ":" + fish.getName();

			report.setRecentFish(fishID);
			report.incrementFishCaught(1);
			report.incrementTotalLength(fish.getLength());
			if (report.getFirstFish().equals("None")) {
				report.setFirstFish(fishID);
			}
			if (fish.getLength() > report.getLargestLength()) {
				report.setLargestFish(fishID);
				report.setLargestLength(fish.getLength());
			}
		}
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

		File oldDataFolder = new File(JavaPlugin.getProvidingPlugin(DatabaseV3.class).getDataFolder() + "/data/");
		File dataFolder = new File(JavaPlugin.getProvidingPlugin(DatabaseV3.class).getDataFolder() + "/data-archived/");

		if (oldDataFolder.renameTo(dataFolder)) {
			Message message = new Message("Archived /data/ folder.");
			message.usePrefix(PrefixType.ADMIN);
			message.broadcast(initiator, true, false);
		} else {
			Message message = new Message("Failed to archive /data/ folder. Cancelling migration. [No further information]");
			message.usePrefix(PrefixType.ADMIN);
			message.broadcast(initiator, true, false);
			return;
		}

		Message fishReportMSG = new Message("Beginning FishReport migrations. This may take a while.");
		fishReportMSG.usePrefix(PrefixType.ADMIN);
		fishReportMSG.broadcast(initiator, true, false);

		try {
			translateFishDataV2();
			createTables(true);

			for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
				Type fishReportList = new TypeToken<List<FishReport>>(){}.getType();

				Gson gson = new Gson();
				FileReader reader = new FileReader(file);
				List<FishReport> reports = gson.fromJson(new FileReader(file), fishReportList);
				UUID playerUUID = UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf(".")));
				reader.close();
				createUser(playerUUID);
				translateFishReportsV2(playerUUID, reports);

				Message migratedMSG = new Message("Migrated " + reports.size() + " fish for: " + playerUUID);
				migratedMSG.usePrefix(PrefixType.ADMIN);
				migratedMSG.broadcast(initiator, true, false);
			}

		} catch (NullPointerException | SQLException | FileNotFoundException exception) {
			exception.printStackTrace();
			Message message = new Message("Fatal error whilst upgrading to V3 database engine.");
			message.usePrefix(PrefixType.ERROR);
			message.broadcast(initiator, true, false);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		Message migratedMSG = new Message("Migration completed. Your database is now using the V3 database engine: you do" +
				" not need to restart or reload your server to complete the process.");
		migratedMSG.usePrefix(PrefixType.ERROR);
		migratedMSG.broadcast(initiator, true, false);

		Message thankyou = new Message("Now that migration is complete, you will be able to use functionality in upcoming" +
				" updates such as quests, deliveries and a fish log. - Oheers");
		thankyou.usePrefix(PrefixType.ERROR);
		thankyou.broadcast(initiator, true, false);

		this.usingV2 = false;
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
		if (queryTableExistence("Fish2", this.connection)) {
			sendStatement("ALTER TABLE Fish2 RENAME TO " + Table.EMF_FISH.getTableID() + ";", this.connection);
		} else {
			sendStatement(Table.EMF_FISH.creationCode, this.connection);
		}

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
				try (PreparedStatement prep = connection.prepareStatement(emfFishLogSQL)) {
					prep.execute();
				}
			} catch (SQLException exception) {
				EvenMoreFish.logger.log(Level.SEVERE, "Could not add " + uuid + " in the table: Users.");
				exception.printStackTrace();
			}
		}

		String emfUsersSQL = "UPDATE emf_users SET first_fish = ?, largest_fish = ?, num_fish_caught = ?, largest_length = ? WHERE uuid = ?;";

		// starts a field for the new fish that's been fished for the first time
		try {
			try (PreparedStatement prep = connection.prepareStatement(emfUsersSQL)) {
				prep.setString(1, firstFishID);
				prep.setString(2, largestFishID);
				prep.setInt(3, totalFish);
				prep.setFloat(4, largestSize);
				prep.setString(5, uuid.toString());

				prep.execute();
			}
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

		ResultSet resultSet;
		try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM emf_users WHERE uuid = ?;")) {
			statement.setString(1, uuid.toString());
			resultSet = statement.executeQuery();
		}

		if (resultSet.next()) {
			return resultSet.getInt("id");
		} else {
			return 0;
		}
	}

	/**
	 * Writes a finished competition to the live database. The database does not need to be opened and closed as it's
	 * done within this method. If the competition has no users in the leaderboard, "None" is put into the columns where
	 * there would need to be leaderboard data.
	 *
	 * @param competition The competition object, this must have finished to record accurate data. An ID is auto-generated.
	 */
	public void createCompetitionReport(@NotNull final Competition competition) {
		String sql = "INSERT INTO emf_competitions (competition_name, winner_uuid, winner_fish, winner_score, contestants) " +
				"VALUES (?, ?, ?, ?, ?);";

		// starts a field for the new fish that's been fished for the first time
		try {
			getConnection();
			Leaderboard leaderboard = competition.getLeaderboard();
			try (PreparedStatement prep = connection.prepareStatement(sql)) {
				prep.setString(1, competition.getCompetitionName());
				if (leaderboard.getSize() > 0) {
					prep.setString(2, leaderboard.getTopEntry().getPlayer().toString());
					Fish topFish = leaderboard.getPlaceFish(1);
					prep.setString(3, topFish.getRarity().getValue() + ":" + topFish.getName());
					prep.setFloat(4, leaderboard.getTopEntry().getValue());
					StringBuilder contestants = new StringBuilder();
					for (CompetitionEntry entry : leaderboard.getEntries()) {
						contestants.append(entry.getPlayer()).append(",");
					}
					// Removes the last ,
					prep.setString(5, contestants.substring(0, contestants.length() - 1));
				} else {
					prep.setString(2, "\"None\"");
					prep.setString(3, "\"None\"");
					prep.setFloat(4, 0);
					prep.setString(5, "\"None\"");
				}

				prep.execute();
			}
			closeConnection();
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not add the current competition in the table: emf_competitions.");
			exception.printStackTrace();
		}

		if (EvenMoreFish.mainConfig.doDBVerbose()) {
			EvenMoreFish.logger.log(Level.INFO, "Written competition report for (" + competition.getCompetitionName() + ") to the database.");
		}
	}

	/**
	 * Creates an empty user field for the user in the database. Their ID is created by an auto increment and all first,
	 * last and largest fish are set to "None"
	 *
	 * @param uuid The user field to be created.
	 */
	public void createUser(UUID uuid) {
		String sql = "INSERT INTO emf_users (uuid, first_fish, last_fish, largest_fish, largest_length, num_fish_caught, total_fish_length," +
				"competitions_won, competitions_joined) VALUES (?, \"None\",\"None\",\"None\", 0, 0, 0, 0, 0);";

		// starts a field for the new fish that's been fished for the first time
		try {
			getConnection();
			try (PreparedStatement prep = connection.prepareStatement(sql)) {
				prep.setString(1, uuid.toString());
				prep.execute();
			}
			closeConnection();
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not add " + uuid + " in the table: Users.");
			exception.printStackTrace();
		}

		if (EvenMoreFish.mainConfig.doDBVerbose()) {
			EvenMoreFish.logger.log(Level.INFO, "Written empty user report for (" + uuid + ") to the database.");
		}
	}

	/**
	 * Queries the database to find out whether the user is present within the table provided as a parameter. Only
	 * the emf_users and emf_fish_log can be passed through, otherwise InvalidTableException will be thrown.
	 *
	 * @param uuid The user being queried.
	 * @param table The table being queried.
	 * @param closeConnection If the connection should be closed after use.
	 * @return If the database contains the user.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 * @throws InvalidTableException The table specified is not emf_users or emf_fish_log
	 */
	public boolean hasUser(@NotNull final UUID uuid, @NotNull final Table table, final boolean closeConnection) throws SQLException, InvalidTableException {
		getConnection();

		try {
			if (table == Table.EMF_FISH_LOG) {
				if (!hasUser(uuid, Table.EMF_USERS, false)) return false;

				int userID = getUserID(uuid);
				try (PreparedStatement prep = connection.prepareStatement("SELECT * FROM emf_fish_log WHERE id = ?;")) {
					prep.setInt(1, userID);

					return prep.executeQuery().next();
				}
			} else if (table == Table.EMF_USERS) {
				try(PreparedStatement prep = connection.prepareStatement("SELECT * FROM emf_users WHERE uuid = ?;")) {
					prep.setString(1, uuid.toString());

					return prep.executeQuery().next();
				}
			} else {
				throw new InvalidTableException(table.tableID + " is not an allowed table type to query user existence.");
			}
		} finally {
			if (closeConnection) closeConnection();
		}
	}

	/**
	 * Obtains fish report objects from the database. There must have been prior checks to make sure that the player does
	 * actually exist in the emf_fish_log database.
	 *
	 * @param uuid The UUID of the user, NOT the id stored in emf_users.
	 * @return A list of fish reports associated with the user.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	public List<FishReport> getFishReports(@NotNull final UUID uuid) throws SQLException {
		getConnection();

		int userID = getUserID(uuid);
		ResultSet resultSet;
		try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM emf_fish_log WHERE id = ?")) {
			statement.setInt(1, userID);
			resultSet = statement.executeQuery();
		}

		List<FishReport> reports = new ArrayList<>();
		while (resultSet.next()) {
			FishReport report = new FishReport(
					resultSet.getString("rarity"),
					resultSet.getString("fish"),
					resultSet.getFloat("largest_length"),
					resultSet.getInt("quantity")
			);
			reports.add(report);
		}

		try {
			return reports;
		} finally {
			closeConnection();

			if (EvenMoreFish.mainConfig.doDBVerbose()) {
				EvenMoreFish.logger.log(Level.INFO, "Read fish reports for (" + uuid + ") from the database.");
			}
		}
	}

	/**
	 * Queries whether the users have already fished the specific fished registered in the fish report. The database
	 * connection must already be opened and closed, and is passed through to be used by the method.
	 *
	 * @param rarity The rarity string of the fish.
	 * @param fish The name string of the fish.
	 * @param id The user's id stored in the emf_user table.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 * @return If the user has already caught this fish, registering it into the database.
	 */
	public boolean userHasFish(@NotNull final String rarity, @NotNull final String fish, final int id, final Connection conn) throws SQLException {
		try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM emf_fish_log WHERE id = ? AND rarity = ? AND fish = ?")) {
			statement.setInt(1, id);
			statement.setString(2, rarity);
			statement.setString(3, fish);

			return statement.executeQuery().next();
		}
	}

	/**
	 * Adds a new fish to the emf_fish_log fish database. There must not be a fish already with this name and rarity already
	 * present within the database. The connection passed through must be opened and closed as this method only uses
	 * the connection rather than managing it.
	 *
	 * @param report The report to be saved to the database
	 * @param conn The connection to be used by the method
	 * @param userID The id of the user found in the emf_users table.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	public void addUserFish(@NotNull final FishReport report, @NotNull final Connection conn, final int userID) throws SQLException {
		try (PreparedStatement statement = conn.prepareStatement("INSERT INTO emf_fish_log (id, rarity, fish, quantity, " +
				"first_catch_time, largest_length) VALUES (?,?,?,?,?,?);")) {
			statement.setInt(1, userID);
			statement.setString(2, report.getRarity());
			statement.setString(3, report.getName());
			statement.setInt(4, report.getNumCaught());
			statement.setLong(5, report.getTimeEpoch());
			statement.setFloat(6, report.getLargestLength());

			statement.execute();
		}
		if (EvenMoreFish.mainConfig.doDBVerbose()) {
			EvenMoreFish.logger.log(Level.INFO, "Written first user fish log data for (userID:" + userID + ") for (" + report.getName() + ") to the database.");
		}
	}

	/**
	 * Updates an existing field for a database in the emf_fish_log by writing a fishing report to the data. The user must
	 * have already caught the fish referenced by the fish report, otherwise an SQL Exception will be thrown.
	 *
	 * @param report The report to be written.
	 * @param conn The connection to be used by the method.
	 * @param userID The id of the user found in the emf_users table.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	public void updateUserFish(@NotNull final FishReport report, @NotNull final Connection conn, final int userID) throws SQLException {
		try (PreparedStatement statement = conn.prepareStatement("UPDATE emf_fish_log SET quantity = ?, largest_length = ? " +
				"WHERE id = ? AND rarity = ? AND fish = ?;")) {
			statement.setInt(1, report.getNumCaught());
			statement.setFloat(2, report.getLargestLength());
			statement.setInt(3, userID);
			statement.setString(4, report.getRarity());
			statement.setString(5, report.getName());

			statement.execute();
		}
	}

	/**
	 * Writes data to the live database, checking whether the user has already fished that fish and updating their records
	 * if necessary or inserting the new data if it doesn't exist. The data being passed through must have been read from
	 * the database at some point otherwise there could be data simply removed.
	 *
	 * @param uuid The user having their data updated.
	 * @param reports The report data which is being written to the database.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	public void writeFishReports(@NotNull final UUID uuid, @NotNull final List<FishReport> reports) throws SQLException {
		getConnection();

		int userID = getUserID(uuid);
		for (FishReport report : reports) {
			if (userHasFish(report.getRarity(), report.getName(), userID, this.connection)) {
				updateUserFish(report, this.connection, userID);
			} else {
				addUserFish(report, this.connection, userID);
			}
		}

		if (EvenMoreFish.mainConfig.doDBVerbose()) {
			EvenMoreFish.logger.log(Level.INFO, "Updated user report for (userID:" + userID + ") in the database.");
		}

		closeConnection();
	}

	/**
	 * Writes data stored in a user report to the database. The user must already have a field created for them in the
	 * database and the report must not be null. The data being passed through must have been read from the database at
	 * some point otherwise there could be data simply removed.
	 *
	 * @param uuid The uuid of the user owning the report.
	 * @param report The report to be written to the database.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 */
	public void writeUserReport(@NotNull final UUID uuid, @NotNull final UserReport report) throws SQLException {
		getConnection();
		//todo sometimes UserReport is null
		try(PreparedStatement statement = this.connection.prepareStatement("UPDATE emf_users SET first_fish = ?, last_fish = ?, " +
				"largest_fish = ?, largest_length = ?, num_fish_caught = ?, total_fish_length = ?, competitions_won = ?, competitions_joined = ? " +
				"WHERE uuid = ?;")) {
			try {
				statement.setString(1, report.getFirstFish());
				statement.setString(2, report.getRecentFish());
				statement.setString(3, report.getLargestFish());
				statement.setFloat(4, report.getLargestLength());
				statement.setInt(5, report.getNumFishCaught());
				statement.setFloat(6, report.getTotalFishLength());
				statement.setInt(7, report.getCompetitionsWon());
				statement.setInt(8, report.getCompetitionsJoined());
				statement.setString(9, uuid.toString());
			} catch (NullPointerException exception) {
				EvenMoreFish.logger.log(Level.SEVERE, "Could not write user data for " + uuid + ", stored users (" + EvenMoreFish.userReports.size() + "/" + Bukkit.getServer().getOnlinePlayers().size() + ")");
				exception.printStackTrace();
			}

			if (EvenMoreFish.mainConfig.doDBVerbose()) {
				EvenMoreFish.logger.log(Level.INFO, "Written user report for (" + uuid + ") to the database.");
			}

			statement.execute(); //todo error here
		}
		closeConnection();
	}

	/**
	 * Reads the data stored in emf_users to create a relevant user report object. This does not contain the user's uuid
	 * but instead their id, their uuid must be stored separately, the plugin does this by default by storing a hashmap
	 * of user uuids : user reports in the EvenMoreFish class.
	 *
	 * @param uuid The uuid of the user being fetched from the database.
	 * @throws SQLException Something went wrong when carrying out SQL instructions.
	 * @return A user report detailing their fishing history on this server. If the user is not present, null is returned.
	 */
	public UserReport readUserReport(@NotNull final UUID uuid) throws SQLException {
		getConnection();

		ResultSet resultSet;

		try (PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM emf_users WHERE uuid = ?")) {
			statement.setString(1, uuid.toString());
			resultSet = statement.executeQuery();
		}

		try {
			if (resultSet.next()) {
				return new UserReport(
						resultSet.getInt("id"),
						resultSet.getInt("num_fish_caught"),
						resultSet.getInt("competitions_won"),
						resultSet.getInt("competitions_joined"),
						resultSet.getString("first_fish"),
						resultSet.getString("last_fish"),
						resultSet.getString("largest_fish"),
						resultSet.getFloat("total_fish_length"),
						resultSet.getFloat("largest_length")
				);
			} else {
				return null;
			}
		} finally {
			closeConnection();

			if (EvenMoreFish.mainConfig.doDBVerbose()) {
				EvenMoreFish.logger.log(Level.INFO, "Read user report for (" + uuid + ") from the database.");
			}
		}
	}

	/**
	 * Registers a new fish within the emf_fish table. This is exactly the same as the V2 database engine and the new
	 * value is simply inserted into the table and relevant data such as the first fisher is recorded down based on the
	 * UUID provided. This fish must not have already existed within the database.
	 *
	 * @param fish The fish to be registered
	 * @param uuid The first person to have caught this fish.
	 */
	public void createFishData(@NotNull final Fish fish, @NotNull final UUID uuid) {
		try {
			getConnection();

			String sql = "INSERT INTO emf_fish (fish_name, fish_rarity, first_fisher, total_caught, largest_fish, largest_fisher, first_catch_time) VALUES (?,?,?,?,?,?,?);";

			// starts a field for the new fish that's been fished for the first time
			try (PreparedStatement prep = connection.prepareStatement(sql)) {
				prep.setString(1, fish.getName());
				prep.setString(2, fish.getRarity().getValue());
				prep.setString(3, uuid.toString());
				prep.setDouble(4, 1);
				prep.setFloat(5, Math.round(fish.getLength()*10f)/10f);
				prep.setString(6, uuid.toString());
				prep.setLong(7, Instant.now().getEpochSecond());
				prep.execute();
			}

			closeConnection();
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not add " + fish.getName() + " to the database.");
			exception.printStackTrace();
		}
	}

	/**
	 * Queries whether a fish has been stored in the emf_fish database. This information can be used to modify data
	 * such as when the user catches a fish or if for whatever reason you needed to remove numbers of fish caught. It
	 * must be done before making any changes to data that is assumed to be in the database.
	 *
	 * @param fish The fish being queried.
	 * @returns If the fish is present or not. If an SQLException occurs, true will be returned.
	 */
	public boolean hasFishData(@NotNull final Fish fish) {
		try {
			getConnection();

			try (PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM emf_fish WHERE fish_name = ? AND fish_rarity = ?")) {
				statement.setString(1, fish.getName());
				statement.setString(2, fish.getRarity().getValue());

				try {
					return statement.executeQuery().next();
				} finally {
					closeConnection();
				}
			}
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not check if " + fish.getName() + " is present in the database.");
			exception.printStackTrace();
			return true;
		}
	}

	/**
	 * Increases the number of total catches the fish has on the server's database. The fish must already exist within
	 * the emf_fish table.
	 *
	 * @param fish The fish to be increased.
	 */
	public void incrementFish(@NotNull final Fish fish) {
		try {
			getConnection();

			String sql = "UPDATE emf_fish SET total_caught = total_caught + 1 WHERE fish_rarity = ? AND fish_name = ?;";

			try(PreparedStatement prep = connection.prepareStatement(sql)) {
				prep.setString(1, fish.getRarity().getValue());
				prep.setString(2, fish.getName());
				prep.execute();
			}
			closeConnection();
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not check if " + fish.getName() + " is present in the database.");
			exception.printStackTrace();
		}
	}

	/**
	 * Gets the largest fish length from the emf_fish database. The fish must already exist within the database and this
	 * can be checked using hasFishData().
	 *
	 * @param fish The fish being queried.
	 * @return The float length of the largest fish ever caught in the server's database history. If an error occurs, the
	 * 		   max float value is returned.
	 */
	public float getLargestFishSize(@NotNull final Fish fish) {
		try {
			getConnection();

			String sql = "SELECT largest_fish FROM emf_fish WHERE fish_rarity = ? AND fish_name = ?;";

			try (PreparedStatement prep = connection.prepareStatement(sql)) {
				prep.setString(1, fish.getRarity().getValue());
				prep.setString(2, fish.getName());
				ResultSet resultSet = prep.executeQuery();
				try {
					if (resultSet.next()) {
						return resultSet.getFloat("largest_fish");
					}
				} finally {
					closeConnection();
				}
			}
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not check for " + fish.getName() + "'s largest fish size.");
			exception.printStackTrace();
		}

		return Float.MAX_VALUE;
	}

	/**
	 * Updates the value for the largest fish length and the player that caught said fish in the emf_fish table. The fish
	 * must already exist within the table otherwise an SQLException will be called and the task will fail.
	 *
	 * @param fish The fish having its length updated, the length stored in this object will be used to update.
	 * @param uuid The uuid of the player who caught the fish.
	 */
	public void updateLargestFish(@NotNull final Fish fish, @NotNull final UUID uuid) {
		try {
			getConnection();

			String sql = "UPDATE emf_fish SET largest_fish = ?, largest_fisher = ? WHERE fish_rarity = ? AND fish_name = ?;";

			float roundedFloatLength = Math.round(fish.getLength() * 10f) / 10f;
			try (PreparedStatement prep = connection.prepareStatement(sql)) {
				prep.setFloat(1, roundedFloatLength);
				prep.setString(2, uuid.toString());
				prep.setString(3, fish.getRarity().getValue());
				prep.setString(4, fish.getName());
				prep.execute();
			}
			closeConnection();
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Could not update for " + fish.getName() + "'s largest fish size.");
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
		this.isMySQL = EvenMoreFish.mainConfig.isMysql();
		this.URL = fetchURL();

		try {
			createTables(false);
		} catch (SQLException exception) {
			EvenMoreFish.logger.log(Level.SEVERE, "Failed to create new tables or check for present tables.");
			exception.printStackTrace();
		}
	}
}
