package com.oheers.fish.database;

import com.devskiller.friendly_id.FriendlyId;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.Leaderboard;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.connection.ConnectionFactory;
import com.oheers.fish.database.connection.MySqlConnectionFactory;
import com.oheers.fish.database.connection.SqliteConnectionFactory;
import com.oheers.fish.database.migrate.LegacyToV3DatabaseMigration;
import com.oheers.fish.exceptions.InvalidTableException;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class DatabaseV3 {
    private boolean usingV2;
    private final ConnectionFactory connectionFactory;
    
    public void setUsingV2(boolean usingV2) {
        this.usingV2 = usingV2;
    }
    
    /**
     * This is a reference to all database activity within the EMF plugin. It improves on the previous DatabaseV2 in that
     * when the config states that data must be stored in MySQL it won't then go and store the data in flat-file format.
     * The database structure contains three tables, emf_users, emf_fish and emf_competitions. The emf_users table is used
     * to store the data that makes up UserReports. The emf_fish table stores data to make up a FishReport and you guessed it,
     * the emf_competitions stores data for a CompetitionReport.
     * <p>
     * In DatabaseV2 there were just fish reports which was all well and good, but they were always stored in flat-file, and
     * it was nearly impossible to store accurate data on a user. The CompetitionReports store each competition with its
     * own "id", this is unique to every single competition, even ones with the same name, same time but a different week.
     * Data such as who won them, which fish won and the total number of participants is stored there.
     *
     * @param plugin An instance of the JavaPlugin extended main class.
     */
    public DatabaseV3(@NotNull EvenMoreFish plugin) {
        this.usingV2 = Files.isDirectory(Paths.get(plugin.getDataFolder() + "/data/"));
    
        if (MainConfig.getInstance().isMysql() && hasCredentials()) {
            this.connectionFactory = new MySqlConnectionFactory();
        } else {
            this.connectionFactory = new SqliteConnectionFactory();
        }
    
        this.connectionFactory.init();
    }
    
    private boolean hasCredentials() {
        return MainConfig.getInstance().getUsername() != null &&
            MainConfig.getInstance().getPassword() != null &&
            MainConfig.getInstance().getAddress() != null &&
            MainConfig.getInstance().getDatabase() != null;
    }

    /**
     * Creates a connection to the database to send/receive data. This must be closed with the #closeConnection() method
     * once the connection is no longer needed.
     * Should only be used internally in this class.
     * If you need to access the database, please add a method in this class for it.
     * @throws SQLException Something went wrong when carrying out SQL instructions.
     */
    private Connection getConnection() throws SQLException {
        return this.connectionFactory.getConnection();
    }
    
    public void shutdown() {
        try {
            this.connectionFactory.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * This gets the database metadata and queries whether the table exists using the #getTables method. The connection
     * passed through must be open.
     *
     * @param tableID The name of the table to be queried.
     * @return If the table exists, true will be returned. If it doesn't, or there is an error, false is returned.
     */
    public boolean queryTableExistence(@NotNull final String tableID) {
        return Boolean.TRUE.equals(getStatement(f -> {
            try {
                DatabaseMetaData dbMetaData = f.getMetaData();
                try (ResultSet resultSet = dbMetaData.getTables(null, null, tableID, null)) {
                    return resultSet.next();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }));
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
     */
    public void createTables(final boolean overrideV2Check)  {
        if (!overrideV2Check && usingVersionV2()) {
            EvenMoreFish.getInstance().getLogger().severe("Your server is running EMF database V2. To continue using database functionality you need to run /emf migrate.");
            return;
        }

        //should be done via flyway possibly?
        for (Table table : Table.values()) {
            if (queryTableExistence(table.getTableID()) ||
                table.getCreationCode() == null ||
                (MainConfig.getInstance().isMysql() && !table.isMySQLCompatible))
                continue;
            
            executeStatement(c -> {
                try(PreparedStatement statement = c.prepareStatement(table.getCreationCode())) {
                    statement.execute();
                } catch (SQLException e) {
                    EvenMoreFish.getInstance().getLogger().warning("There was a problem creating the table.");
                }
            });
        }
    }

    private List<FishReport> getCachedReportsOrReports(final UUID uuid, final Fish fish) {
        List<FishReport> cachedReports = DataManager.getInstance().getFishReportsIfExists(uuid);
        if(cachedReports == null) {
            return new ArrayList<>(Collections.singletonList(
                new FishReport(
                    fish.getRarity().getValue(),
                    fish.getName(),
                    fish.getLength(),
                    1,
                    -1
                )
            ));
        }
    
        for (FishReport report : cachedReports) {
            if (report.getRarity().equals(fish.getRarity().getValue()) && report.getName().equals(fish.getName())) {
                report.addFish(fish);
                return cachedReports;
            }
        }
    
        cachedReports.add(
            new FishReport(
                fish.getRarity().getValue(),
                fish.getName(),
                fish.getLength(),
                1,
                -1
            )
        );
        return cachedReports;
        
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
        List<FishReport> cachedReports = getCachedReportsOrReports(uuid,fish);
        DataManager.getInstance().putFishReportsCache(uuid, cachedReports);
        
        UserReport report = DataManager.getInstance().getUserReportIfExists(uuid);

        if (report != null) {
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

            DataManager.getInstance().putUserReportCache(uuid, report);
        }
    }

    /**
     * Converts a V2 database system to a V3 database system. The server must not crash during this process as this may
     * lead to data loss, but honestly I'm not 100% sure on that one. Data is read from the /data/ folder and is
     * inserted into the new database system then the /data/ folder is renamed to /data-old/.
     *
     * @param initiator The person who started the migration.
     *
     */
    public void migrateLegacy(CommandSender initiator) {
        LegacyToV3DatabaseMigration legacy = new LegacyToV3DatabaseMigration(this);
        legacy.migrate(initiator);
    }

    

    /**
     * Returns the user's ID from the emf_users table. If there is no user, the value 0 will be returned to indicate
     * that the user is not yet present in the table. This ID can be used for the emf_fish_log table which is used
     * to store the user's fish log.
     *
     * @param uuid The UUID of the user being queried.
     * @return The ID of the user for the database, 0 if there is no user present matching the UUID.
     */
    public int getUserID(@NotNull final UUID uuid) {
        Integer userId = getStatement(f -> {
            try (PreparedStatement statement = f.prepareStatement("SELECT id FROM emf_users WHERE uuid = ?;")) {
                statement.setString(1, uuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    }
                    return null;
                }
            } catch (SQLException e) {
                return null;
            }
        });
       
        return userId == null ? 0 : userId;
    }

    /**
     * Writes a finished competition to the live database. The database does not need to be opened and closed as it's
     * done within this method. If the competition has no users in the leaderboard, "None" is put into the columns where
     * there would need to be leaderboard data.
     *
     * @param competition The competition object, this must have finished to record accurate data. An ID is auto-generated.
     */
    public void createCompetitionReport(@NotNull final Competition competition) {
        final String none = "\"None\"";
        final String sql = "INSERT INTO emf_competitions (competition_name, winner_uuid, winner_fish, winner_score, contestants) " +
                "VALUES (?, ?, ?, ?, ?);";
        
        // starts a field for the new fish that's been fished for the first time
        executeStatement(c -> {
            try (PreparedStatement prep = c.prepareStatement(sql)){
                Leaderboard leaderboard = competition.getLeaderboard();
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
                    prep.setString(2, none);
                    prep.setString(3, none);
                    prep.setFloat(4, 0);
                    prep.setString(5, none);
                }
        
                prep.executeUpdate();
    
                if (MainConfig.getInstance().doDBVerbose()) {
                    EvenMoreFish.getInstance().getLogger().info(() ->  "Written competition report for (" + competition.getCompetitionName() + ") to the database.");
                }
                
            } catch (SQLException exception) {
                EvenMoreFish.getInstance().getLogger().severe(() -> "Could not add the current competition ("+competition.getCompetitionName()+") in the table: emf_competitions.");
                exception.printStackTrace();
            }
        });

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
        executeStatement(c -> {
            try (PreparedStatement prep = c.prepareStatement(sql)){
                prep.setString(1, uuid.toString());
                prep.executeUpdate();
        
                if (MainConfig.getInstance().doDBVerbose()) {
                    EvenMoreFish.getInstance().getLogger().info(() -> "Written empty user report for (" + uuid + ") to the database.");
                }
            } catch (SQLException exception) {
                EvenMoreFish.getInstance().getLogger().severe(() -> "Could not add " + uuid + " in the table: Users.");
                exception.printStackTrace();
            }
        });
    }

    /**
     * Queries the database to find out whether the user is present within the table provided as a parameter. Only
     * the emf_users and emf_fish_log can be passed through, otherwise InvalidTableException will be thrown.
     *
     * @param uuid  The user being queried.
     * @param table The table being queried.
     * @return If the database contains the user.
     * @throws InvalidTableException The table specified is not emf_users or emf_fish_log
     */
    public boolean hasUser(@NotNull final UUID uuid, @NotNull final Table table) throws InvalidTableException {
        if (table == Table.EMF_FISH_LOG) {
            if (!hasUser(uuid, Table.EMF_USERS))
                return false;
        
            int userID = getUserID(uuid);
            return Boolean.TRUE.equals(getStatement(c -> {
                try (PreparedStatement prep = c.prepareStatement("SELECT * FROM emf_fish_log WHERE id = ?;")) {
                    prep.setInt(1, userID);
                    return prep.executeQuery().next();
                } catch (SQLException e) {
                    return false;
                }
            }));
        }
        
        if (table == Table.EMF_USERS) {
            return Boolean.TRUE.equals(getStatement(c -> {
                try (PreparedStatement prep = getConnection().prepareStatement("SELECT * FROM emf_users WHERE uuid = ?;")) {
                    prep.setString(1, uuid.toString());
                    return prep.executeQuery().next();
                } catch (SQLException e) {
                    return false;
                }
            }));
            
        }
        
        throw new InvalidTableException(table.tableID + " is not an allowed table type to query user existence.");
    }

    /**
     * Obtains fish report objects from the database. There must have been prior checks to make sure that the player does
     * actually exist in the emf_fish_log database.
     *
     * @param uuid The UUID of the user, NOT the id stored in emf_users.
     * @return A list of fish reports associated with the user.
     */
    public List<FishReport> getFishReports(@NotNull final UUID uuid) {
        int userID = getUserID(uuid);
        
        return getStatement(f -> {
            try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM emf_fish_log WHERE id = ?")) {
                statement.setInt(1, userID);
        
                List<FishReport> reports = new ArrayList<>();
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        FishReport report = new FishReport(
                            resultSet.getString("rarity"),
                            resultSet.getString("fish"),
                            resultSet.getFloat("largest_length"),
                            resultSet.getInt("quantity"),
                            resultSet.getLong("first_catch_time")
                        );
                        reports.add(report);
                    }
                }
        
                if (MainConfig.getInstance().doDBVerbose()) {
                    EvenMoreFish.getInstance().getLogger().info(() -> "Read fish reports for (" + uuid + ") from the database.");
                }
        
                return reports;
            } catch (SQLException e) {
                EvenMoreFish.getInstance().getLogger().warning("There was a problem reading reports for ("+uuid+")");
                e.printStackTrace();
                return Collections.emptyList();
            }
        });
    }

    /**
     * Queries whether the users have already fished the specific fished registered in the fish report. The database
     * connection must already be opened and closed, and is passed through to be used by the method.
     *
     * @param rarity The rarity string of the fish.
     * @param fish   The name string of the fish.
     * @param id     The user's id stored in the emf_user table.
     * @return If the user has already caught this fish, registering it into the database.
     */
    public boolean userHasFish(@NotNull final String rarity, @NotNull final String fish, final int id) {
        return Boolean.TRUE.equals(getStatement(f -> {
            try (PreparedStatement statement = this.getConnection().prepareStatement("SELECT * FROM emf_fish_log WHERE id = ? AND rarity = ? AND fish = ?")) {
                statement.setInt(1, id);
                statement.setString(2, rarity);
                statement.setString(3, fish);
                return statement.executeQuery().next();
            } catch (SQLException e) {
                return false;
            }
        }));
    }

    /**
     * Adds a new fish to the emf_fish_log fish database. There must not be a fish already with this name and rarity already
     * present within the database. The connection passed through must be opened and closed as this method only uses
     * the connection rather than managing it.
     *
     * @param report The report to be saved to the database
     * @param userID The id of the user found in the emf_users table.
     */
    public void addUserFish(@NotNull final FishReport report, final int userID) {
        executeStatement(c -> {
            try (PreparedStatement statement = c.prepareStatement("INSERT INTO emf_fish_log (id, rarity, fish, quantity, " +
                "first_catch_time, largest_length) VALUES (?,?,?,?,?,?);")) {
                statement.setInt(1, userID);
                statement.setString(2, report.getRarity());
                statement.setString(3, report.getName());
                statement.setInt(4, report.getNumCaught());
                statement.setLong(5, report.getTimeEpoch());
                statement.setFloat(6, report.getLargestLength());
        
                statement.executeUpdate();
    
                if (MainConfig.getInstance().doDBVerbose()) {
                    EvenMoreFish.getInstance().getLogger().info(() -> "Written first user fish log data for (userID:" + userID + ") for (" + report.getName() + ") to the database.");
                }
            } catch (SQLException e) {
                EvenMoreFish.getInstance().getLogger().warning("There was a problem adding a new fish for (userID:" + userID+ ")");
                e.printStackTrace();
            }
        });
    }

    /**
     * Updates an existing field for a database in the emf_fish_log by writing a fishing report to the data. The user must
     * have already caught the fish referenced by the fish report, otherwise an SQL Exception will be thrown.
     *
     * @param report The report to be written.
     * @param userID The id of the user found in the emf_users table.
     */
    public void updateUserFish(@NotNull final FishReport report, final int userID) {
        executeStatement(c -> {
            try (PreparedStatement statement = c.prepareStatement("UPDATE emf_fish_log SET quantity = ?, largest_length = ? " +
                "WHERE id = ? AND rarity = ? AND fish = ?;")) {
                statement.setInt(1, report.getNumCaught());
                statement.setFloat(2, report.getLargestLength());
                statement.setInt(3, userID);
                statement.setString(4, report.getRarity());
                statement.setString(5, report.getName());
        
                statement.executeUpdate();
            } catch (SQLException e) {
                EvenMoreFish.getInstance().getLogger().warning("There was a problems setting user's " + userID + "fish.");
                e.printStackTrace();
            }
        });
    }

    /**
     * Writes data to the live database, checking whether the user has already fished that fish and updating their records
     * if necessary or inserting the new data if it doesn't exist. The data being passed through must have been read from
     * the database at some point otherwise there could be data simply removed.
     *
     * @param uuid    The user having their data updated.
     * @param reports The report data which is being written to the database.
     */
    public void writeFishReports(@NotNull final UUID uuid, @NotNull final List<FishReport> reports)  {
        int userID = getUserID(uuid);
        for (FishReport report : reports) {
            if (userHasFish(report.getRarity(), report.getName(), userID)) {
                updateUserFish(report, userID);
            } else {
                addUserFish(report, userID);
            }
        }

        if (MainConfig.getInstance().doDBVerbose()) {
            EvenMoreFish.getInstance().getLogger().info(() -> "Updated user report for (userID:" + userID + ") in the database.");
        }
    }

    /**
     * Writes data stored in a user report to the database. The user must already have a field created for them in the
     * database and the report must not be null. The data being passed through must have been read from the database at
     * some point otherwise there could be data simply removed.
     *
     * @param uuid   The uuid of the user owning the report.
     * @param report The report to be written to the database.
     */
    public void writeUserReport(@NotNull final UUID uuid, @NotNull final UserReport report) {
        executeStatement(c -> {
            try (PreparedStatement statement = c.prepareStatement("UPDATE emf_users SET first_fish = ?, last_fish = ?, " +
                "largest_fish = ?, largest_length = ?, num_fish_caught = ?, total_fish_length = ?, competitions_won = ?, " +
                "competitions_joined = ?, fish_sold = ?, money_earned = ? " +
                "WHERE uuid = ?;")) {
            
                statement.setString(1, report.getFirstFish());
                statement.setString(2, report.getRecentFish());
                statement.setString(3, report.getLargestFish());
                statement.setFloat(4, report.getLargestLength());
                statement.setInt(5, report.getNumFishCaught());
                statement.setFloat(6, report.getTotalFishLength());
                statement.setInt(7, report.getCompetitionsWon());
                statement.setInt(8, report.getCompetitionsJoined());
                statement.setInt(9,report.getFishSold());
                statement.setDouble(10,report.getMoneyEarned());
                statement.setString(11, uuid.toString());
            
            
                if (MainConfig.getInstance().doDBVerbose()) {
                    EvenMoreFish.getInstance().getLogger().info(() -> "Written user report for (" + uuid + ") to the database.");
                }
            
                statement.execute();
            } catch (SQLException e) {
                EvenMoreFish.getInstance().getLogger().warning("Failed to write UserReport due to " + e.getCause());
                e.printStackTrace();
            } catch (NullPointerException e) {
                EvenMoreFish.getInstance().getLogger().severe(() -> "Could not write user report data for " + uuid);
                e.printStackTrace();
            }
        });

    }

    /**
     * Reads the data stored in emf_users to create a relevant user report object. This does not contain the user's uuid
     * but instead their id, their uuid must be stored separately, the plugin does this by default by storing a hashmap
     * of user uuids : user reports in the EvenMoreFish class.
     *
     * @param uuid The uuid of the user being fetched from the database.
     * @return A user report detailing their fishing history on this server. If the user is not present, null is returned.
     */
    public UserReport readUserReport(@NotNull final UUID uuid) {
        return getStatement(f -> {
            try (PreparedStatement statement = f.prepareStatement("SELECT * FROM emf_users WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        if (MainConfig.getInstance().doDBVerbose()) {
                            EvenMoreFish.getInstance().getLogger().info(() -> "Read user report for (" + uuid + ") from the database.");
                        }
                        return new UserReport(
                            resultSet.getInt("id"),
                            resultSet.getInt("num_fish_caught"),
                            resultSet.getInt("competitions_won"),
                            resultSet.getInt("competitions_joined"),
                            resultSet.getString("first_fish"),
                            resultSet.getString("last_fish"),
                            resultSet.getString("largest_fish"),
                            resultSet.getFloat("total_fish_length"),
                            resultSet.getFloat("largest_length"),
                            resultSet.getString("uuid"),
                            resultSet.getInt("fish_sold"),
                            resultSet.getDouble("money_earned")
                        );
                    }
                    if (MainConfig.getInstance().doDBVerbose()) {
                        EvenMoreFish.getInstance().getLogger().info(() ->  "User report for (" + uuid + ") does not exist in the database.");
                    }
                    return null;
                }
            } catch (SQLException e){
                EvenMoreFish.getInstance().getLogger().warning("There was a problem reading the report for "+ uuid);
                e.printStackTrace();
                return null;
            }
        });
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
        String sql = "INSERT INTO emf_fish (fish_name, fish_rarity, first_fisher, total_caught, largest_fish, largest_fisher, first_catch_time) VALUES (?,?,?,?,?,?,?);";
        // starts a field for the new fish that's been fished for the first time
        executeStatement(c -> {
            try (PreparedStatement prep = c.prepareStatement(sql)) {
                prep.setString(1, fish.getName());
                prep.setString(2, fish.getRarity().getValue());
                prep.setString(3, uuid.toString());
                prep.setDouble(4, 1);
                prep.setFloat(5, Math.round(fish.getLength() * 10f) / 10f);
                prep.setString(6, uuid.toString());
                prep.setLong(7, Instant.now().getEpochSecond());
                prep.execute();
            } catch (SQLException exception) {
                EvenMoreFish.getInstance().getLogger().severe(() -> "Could not add " + fish.getName() + " to the database.");
                exception.printStackTrace();
            }
        });
    }

    /**
     * Queries whether a fish has been stored in the emf_fish database. This information can be used to modify data
     * such as when the user catches a fish or if for whatever reason you needed to remove numbers of fish caught. It
     * must be done before making any changes to data that is assumed to be in the database.
     *
     * @param fish The fish being queried.
     * @return If the fish is present or not. If an SQLException occurs, true will be returned.
     */
    public boolean hasFishData(@NotNull final Fish fish) {
        return Boolean.TRUE.equals(getStatement(f -> {
            try (PreparedStatement statement = f.prepareStatement("SELECT * FROM emf_fish WHERE fish_name = ? AND fish_rarity = ?")) {
                statement.setString(1, fish.getName());
                statement.setString(2, fish.getRarity().getValue());
                return statement.executeQuery().next();
            } catch (SQLException exception) {
                EvenMoreFish.getInstance().getLogger().severe(() -> "Could not check if " + fish.getName() + " is present in the database.");
                exception.printStackTrace();
                return true;
            }
        }));
    }

    /**
     * Increases the number of total catches the fish has on the server's database. The fish must already exist within
     * the emf_fish table.
     *
     * @param fish The fish to be increased.
     */
    public void incrementFish(@NotNull final Fish fish) {
        String sql = "UPDATE emf_fish SET total_caught = total_caught + 1 WHERE fish_rarity = ? AND fish_name = ?;";
        executeStatement(c -> {
            try (PreparedStatement prep = c.prepareStatement(sql)) {
                prep.setString(1, fish.getRarity().getValue());
                prep.setString(2, fish.getName());
                prep.execute();
            } catch (SQLException exception) {
                EvenMoreFish.getInstance().getLogger().severe(() -> "Could not check if " + fish.getName() + " is present in the database.");
                exception.printStackTrace();
            }
        });
    }

    /**
     * Gets the largest fish length from the emf_fish database. The fish must already exist within the database and this
     * can be checked using hasFishData().
     *
     * @param fish The fish being queried.
     * @return The float length of the largest fish ever caught in the server's database history. If an error occurs, the
     * max float value is returned.
     */
    public float getLargestFishSize(@NotNull final Fish fish) {
        String sql = "SELECT largest_fish FROM emf_fish WHERE fish_rarity = ? AND fish_name = ?;";
        Float largestFishSize;
        largestFishSize = getStatement(f -> {
            try (PreparedStatement prep = f.prepareStatement(sql)) {
                prep.setString(1, fish.getRarity().getValue());
                prep.setString(2, fish.getName());
                try (ResultSet resultSet = prep.executeQuery()) {
                    return resultSet.getFloat("largest_fish");
                }
            } catch (SQLException exception) {
                EvenMoreFish.getInstance().getLogger().severe(() -> "Could not check for " + fish.getName() + "'s largest fish size.");
                exception.printStackTrace();
                return null;
            }
        });
        return largestFishSize == null ? Float.MAX_VALUE : largestFishSize;
    }

    /**
     * Updates the value for the largest fish length and the player that caught said fish in the emf_fish table. The fish
     * must already exist within the table otherwise an SQLException will be called and the task will fail.
     *
     * @param fish The fish having its length updated, the length stored in this object will be used to update.
     * @param uuid The uuid of the player who caught the fish.
     */
    public void updateLargestFish(@NotNull final Fish fish, @NotNull final UUID uuid) {
        String sql = "UPDATE emf_fish SET largest_fish = ?, largest_fisher = ? WHERE fish_rarity = ? AND fish_name = ?;";

        float roundedFloatLength = Math.round(fish.getLength() * 10f) / 10f;
        executeStatement(c -> {
            try (PreparedStatement prep = c.prepareStatement(sql)) {
                prep.setFloat(1, roundedFloatLength);
                prep.setString(2, uuid.toString());
                prep.setString(3, fish.getRarity().getValue());
                prep.setString(4, fish.getName());
                prep.execute();
            } catch (SQLException exception) {
                EvenMoreFish.getInstance().getLogger().severe(() -> "Could not update for " + fish.getName() + "'s largest fish size.");
                exception.printStackTrace();
            }
        });
    }
    
    //Used a single transaction with multiple sales, optionally.
    public void createSale(final String transactionId, final Timestamp timestamp, final int userId, final String fishName, final String fishRarity, final int fishAmount, final double fishLength, final double priceSold) {
        final String sql =
            "INSERT INTO emf_users_sales (transaction_id, fish_name, fish_rarity, fish_amount, fish_length, price_sold) " +
                "VALUES (?,?,?,?,?,?);";
        
        executeStatement(c -> {
            try (PreparedStatement statement = c.prepareStatement(sql)) {
                statement.setString(1, transactionId);
                statement.setString(2, fishName);
                statement.setString(3, fishRarity);
                statement.setInt(4, fishAmount);
                statement.setDouble(5, fishLength);
                statement.setDouble(6, (Math.floor(priceSold * 10) / 10));
                statement.executeUpdate();
            
                //log in chat?
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    //Used for single sales.
    public void createSale(final int userId, final String fishName, final String fishRarity, final int fishAmount, final double fishLength, final double priceSold) {
        createSale(FriendlyId.createFriendlyId(), Timestamp.from(Instant.now()),userId,fishName,fishRarity,fishAmount,fishLength,priceSold);
    }
    
    /**
     * Creates a new transaction.
     *
     * @param transactionId Unique transaction id
     * @param userId User id
     * @param timestamp timestamp
     */
    public void createTransaction(final String transactionId, final int userId, final Timestamp timestamp) {
        final String sql =
            "INSERT INTO emf_transactions (id, user_id, timestamp) " +
                "VALUES (?,?,?);";
        executeStatement(c -> {
            try(PreparedStatement statement = c.prepareStatement(sql)) {
                statement.setString(1, transactionId);
                statement.setInt(2, userId);
                statement.setTimestamp(3, timestamp);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    
    
    /**
     * This should be used when obtaining data from a database.
     * The connection is closed automatically.
     * For example:
     * <code>
     *     getStatement(f -> {
     *                 try (PreparedStatement prep = f.prepareStatement(sql)) {
     *                     prep.setString(1, fish.getRarity().getValue());
     *                     prep.setString(2, fish.getName());
     *                     try (ResultSet resultSet = prep.executeQuery()) {
     *                         return resultSet.getFloat("largest_fish");
     *                     }
     *                 } catch (SQLException exception) {
     *                     EvenMoreFish.getInstance().getLogger().severe("Could not check for " + fish.getName() + "'s largest fish size.");
     *                     exception.printStackTrace();
     *                     return null;
     *                 }
     *             });
     * </code>
     * Here R is a float.
     * @param func Function to pass.
     * @return A value of whatever R is
     * @param <R> Defined via func
     */
    public  <R> @Nullable R getStatement(@NotNull Function<Connection, R> func){
        try (Connection connection = getConnection()){
            return func.apply(connection);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * This should be used when executing a statement.
     * The connection is closed automatically.
     * @param consumer Function to execute.
     */
    public void executeStatement(@NotNull Consumer<Connection> consumer) {
        try (Connection connection = getConnection()){
            consumer.accept(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
}
