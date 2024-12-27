package com.oheers.fish.database;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.database.connection.ConnectionFactory;
import com.oheers.fish.database.execute.ExecuteQuery;
import com.oheers.fish.database.execute.ExecuteUpdate;
import com.oheers.fish.database.generated.mysql.Tables;
import com.oheers.fish.database.model.FishReport;
import com.oheers.fish.database.model.UserReport;
import com.oheers.fish.fishing.items.Fish;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.conf.*;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Database implements DatabaseWrapper {
    private final Settings settings;
    private final ConnectionFactory connectionFactory;

    public Database(final ConnectionFactory connectionFactory) {
        this.settings = new Settings();
        this.connectionFactory = connectionFactory;
    }

    public void initSettings(final String tablePrefix, final String dbName) {
        settings.setExecuteLogging(true);
        settings.withRenderMapping(
                new RenderMapping().withSchemata(
                        new MappedSchema().withInput("")
                                .withOutput(dbName)
                                .withTables(
                                        new MappedTable()
                                                .withInputExpression(Pattern.compile("\\$\\{table.prefix}(.*)"))
                                                .withOutput(tablePrefix + "$1"
                                                )
                                )

                )
        );
    }

    public void executeStatement(@NotNull Consumer<DSLContext> consumer) {
        try (Connection connection = this.connectionFactory.getConnection()) {
            consumer.accept(this.getContext(connection));
        } catch (SQLException e) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
        }

    }

    private @NotNull DSLContext getContext(Connection connection) {
        return DSL.using(connection, DatabaseUtil.getSQLDialect(this.connectionFactory.getType()), this.settings);
    }

    @Override
    public boolean hasUser(@NotNull UUID uuid) {
        return new ExecuteQuery<Boolean>(connectionFactory) {
            @Override
            protected Boolean onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.select()
                        .from(Tables.USERS)
                        .where(Tables.USERS.UUID.eq(uuid.toString()))
                        .fetch()
                        .isNotEmpty();
            }

            @Override
            protected Boolean empty() {
                return false;
            }
        }.prepareAndRunQuery();
    }

    @Override
    public boolean hasUserLog(@NotNull UUID uuid) {
        final int userId = getUserId(uuid);
        if (userId == 0) {
            return false;
        }

        return new ExecuteQuery<Boolean>(connectionFactory) {
            @Override
            protected Boolean onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.select()
                        .from(Tables.FISH_LOG)
                        .where(Tables.FISH_LOG.ID.eq(userId))
                        .fetch()
                        .isNotEmpty();
            }

            @Override
            protected Boolean empty() {
                return false;
            }
        }.prepareAndRunQuery();
    }

    @Override
    public void createUser(@NotNull UUID uuid) {
        new ExecuteUpdate(connectionFactory) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.USERS)
                        .set(Tables.USERS.UUID, uuid.toString())
                        .set(Tables.USERS.FIRST_FISH, "None")
                        .set(Tables.USERS.LAST_FISH, "None")
                        .set(Tables.USERS.LARGEST_FISH, "None")
                        .set(Tables.USERS.LARGEST_LENGTH, 0F)
                        .set(Tables.USERS.NUM_FISH_CAUGHT, 0)
                        .set(Tables.USERS.TOTAL_FISH_LENGTH, 0F)
                        .execute();
            }
        }.executeUpdate();
    }

    @Override
    public int getUserId(@NotNull UUID uuid) {
        return new ExecuteQuery<Integer>(connectionFactory) {
            @Override
            protected Integer onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.select()
                        .from(Tables.USERS)
                        .where(Tables.USERS.UUID.eq(uuid.toString()))
                        .fetchOne(Tables.USERS.ID)
                        ;
            }

            @Override
            protected Integer empty() {
                return 0;
            }
        }.prepareAndRunQuery();
    }

    @Override
    public void writeUserReport(@NotNull UUID uuid, @NotNull UserReport report) {
        new ExecuteUpdate(connectionFactory) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                int rowsUpdated = dslContext.insertInto(Tables.USERS)
                        .set(Tables.USERS.UUID, uuid.toString())
                        .set(Tables.USERS.FIRST_FISH, report.getFirstFish())
                        .set(Tables.USERS.LAST_FISH, report.getRecentFish())
                        .set(Tables.USERS.LARGEST_FISH, report.getLargestFish())
                        .set(Tables.USERS.LARGEST_LENGTH, report.getLargestLength())
                        .set(Tables.USERS.NUM_FISH_CAUGHT, report.getNumFishCaught())
                        .set(Tables.USERS.TOTAL_FISH_LENGTH, report.getTotalFishLength())
                        .execute();

                DatabaseUtil.writeDbVerbose("Written user report for (%s) to the database.".formatted(uuid));
                return rowsUpdated;
            }
        }.executeUpdate();
    }

    @Override
    public UserReport readUserReport(@NotNull UUID uuid) {
        return new ExecuteQuery<UserReport>(connectionFactory) {
            @Override
            protected UserReport onRunQuery(DSLContext dslContext) throws Exception {
                org.jooq.Record tableRecord = dslContext.select()
                        .from(Tables.USERS)
                        .where(Tables.USERS.UUID.eq(uuid.toString()))
                        .fetchOne();

                DatabaseUtil.writeDbVerbose("Read user report for user (%s)".formatted(uuid.toString()));
                int id = tableRecord.getValue(Tables.USERS.ID);
                int numFishCaught = tableRecord.getValue(Tables.USERS.NUM_FISH_CAUGHT);
                int competitionsWon = tableRecord.getValue(Tables.USERS.COMPETITIONS_WON);
                int competitionsJoined = tableRecord.getValue(Tables.USERS.COMPETITIONS_JOINED);
                String firstFish = tableRecord.getValue(Tables.USERS.FIRST_FISH);
                String lastFish = tableRecord.getValue(Tables.USERS.LAST_FISH);
                String largestFish = tableRecord.getValue(Tables.USERS.LARGEST_FISH);
                float totalFishLength = tableRecord.getValue(Tables.USERS.TOTAL_FISH_LENGTH);
                float largestLength = tableRecord.getValue(Tables.USERS.LARGEST_LENGTH);
                String uuid = tableRecord.getValue(Tables.USERS.UUID);
                int fishSold = tableRecord.getValue(Tables.USERS.FISH_SOLD);
                double moneyEarned = tableRecord.getValue(Tables.USERS.MONEY_EARNED);
                return new UserReport(
                        id,
                        numFishCaught,
                        competitionsWon,
                        competitionsJoined,
                        firstFish,
                        lastFish,
                        largestFish,
                        totalFishLength,
                        largestLength,
                        uuid,
                        fishSold,
                        moneyEarned
                );
            }

            @Override
            protected UserReport empty() {
                DatabaseUtil.writeDbVerbose("User report for (%s) does not exist in the database.".formatted(uuid));
                return null;
            }
        }.prepareAndRunQuery();
    }

    @Override
    public boolean hasFishData(@NotNull Fish fish) {
        return false;
    }

    @Override
    public void createFishData(@NotNull Fish fish, @NotNull UUID uuid) {

    }

    @Override
    public void incrementFish(@NotNull Fish fish) {

    }

    @Override
    public float getLargestFishSize(@NotNull Fish fish) {
        return 0;
    }

    @Override
    public void updateLargestFish(@NotNull Fish fish, @NotNull UUID uuid) {

    }

    @Override
    public List<FishReport> getFishReports(@NotNull UUID uuid) {
        return List.of();
    }

    @Override
    public List<FishReport> getCachedReportsOrReports(@NotNull UUID uuid, @NotNull Fish fish) {
        return List.of();
    }

    @Override
    public void handleFishCatch(@NotNull UUID uuid, @NotNull Fish fish) {

    }

    @Override
    public void addUserFish(@NotNull FishReport report, int userId) {

    }

    @Override
    public void updateUserFish(@NotNull FishReport report, int userId) {

    }

    @Override
    public void writeFishReports(@NotNull UUID uuid, @NotNull List<FishReport> reports) {

    }

    @Override
    public boolean userHasFish(@NotNull String rarity, @NotNull String fish, int id) {
        return false;
    }

    @Override
    public void createCompetitionReport(@NotNull Competition competition) {

    }

    @Override
    public void createSale(@NotNull String transactionId, @NotNull String fishName, @NotNull String fishRarity, int fishAmount, double fishLength, double priceSold) {

    }

    @Override
    public void createTransaction(@NotNull String transactionId, int userId, @NotNull Timestamp timestamp) {

    }
}
