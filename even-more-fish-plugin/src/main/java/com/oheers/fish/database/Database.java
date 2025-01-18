package com.oheers.fish.database;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.connection.*;
import com.oheers.fish.database.execute.ExecuteQuery;
import com.oheers.fish.database.execute.ExecuteUpdate;
import com.oheers.fish.database.generated.mysql.Tables;
import com.oheers.fish.database.generated.mysql.tables.records.CompetitionsRecord;
import com.oheers.fish.database.model.FishReport;
import com.oheers.fish.database.model.UserReport;
import com.oheers.fish.fishing.items.Fish;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.conf.*;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Database implements DatabaseWrapper {
    private final Settings settings;
    private final ConnectionFactory connectionFactory;
    private final MigrationManager migrationManager;

    public Database() {
        this.settings = new Settings();
        this.connectionFactory = getConnectionFactory(MainConfig.getInstance().getDatabaseType().toLowerCase());
        this.connectionFactory.init();

        this.migrationManager = new MigrationManager(connectionFactory);
        if (migrationManager.usingV2()) {
            return;
        }

        final String version = this.migrationManager.getDatabaseVersion().getVersion();
        switch (version) {
            case "5":
                this.migrationManager.migrateFromV5ToLatest();
                break;
            case "6.0":
                this.migrationManager.migrateFromV6ToLatest();
                break;
            default:
                this.migrationManager.migrateFromVersion(version, true);
                break;
        }

        initSettings(MainConfig.getInstance().getPrefix(), MainConfig.getInstance().getDatabase());
    }



    public MigrationManager getMigrationManager() {
        return migrationManager;
    }

    private @NotNull ConnectionFactory getConnectionFactory(final @NotNull String type) {
        return switch(type) {
            case "mysql": yield new MySqlConnectionFactory(); //todo check for credentials.
            case "sqlite": yield new SqliteConnectionFactory();
            default: yield new H2ConnectionFactory();
        };
    }

    public void initSettings(final String tablePrefix, final String dbName) {
        settings.setExecuteLogging(true);

        settings.withRenderMapping(
                new RenderMapping().withSchemata(
                        new MappedSchema()
                                .withInput("") // Leave this if no schema is used
                                .withOutput(dbName)
                                .withTables(
                                        new MappedTable()
                                                .withInputExpression(Pattern.compile("\\$\\{table\\.prefix}(.*)")) // Correct escaped regex
                                                .withOutput(tablePrefix + "$1")
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

    //todo should be in mainconfig, out of scope for this pr
    private boolean hasCredentials() {
        return MainConfig.getInstance().getUsername() != null &&
                MainConfig.getInstance().getPassword() != null &&
                MainConfig.getInstance().getAddress() != null &&
                MainConfig.getInstance().getDatabase() != null;
    }

    private @NotNull DSLContext getContext(Connection connection) {
        return DSL.using(connection, DatabaseUtil.getSQLDialect(this.connectionFactory.getType()), this.settings);
    }

    @Override
    public boolean hasUser(@NotNull UUID uuid) {
        return new ExecuteQuery<Boolean>(connectionFactory, settings) {
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
        if (!hasUser(uuid))
            return false;

        final int userId = getUserId(uuid);
        if (userId == 0) {
            return false;
        }

        return new ExecuteQuery<Boolean>(connectionFactory, settings) {
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
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.USERS)
                        .set(Tables.USERS.UUID, uuid.toString())
                        .set(Tables.USERS.FIRST_FISH, "None")
                        .set(Tables.USERS.LAST_FISH, "None")
                        .set(Tables.USERS.LARGEST_FISH, "None")
                        .set(Tables.USERS.LARGEST_LENGTH, 0F)
                        .set(Tables.USERS.NUM_FISH_CAUGHT, 0)
                        .set(Tables.USERS.COMPETITIONS_WON, 0)
                        .set(Tables.USERS.COMPETITIONS_JOINED, 0)
                        .set(Tables.USERS.TOTAL_FISH_LENGTH, 0F)
                        .execute();
            }
        }.executeUpdate();
    }

    @Override
    public int getUserId(@NotNull UUID uuid) {
        return new ExecuteQuery<Integer>(connectionFactory, settings) {
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
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                int rowsUpdated = dslContext.update(Tables.USERS)
                        .set(Tables.USERS.FIRST_FISH, report.getFirstFish())
                        .set(Tables.USERS.LAST_FISH, report.getRecentFish())
                        .set(Tables.USERS.LARGEST_FISH, report.getLargestFish())
                        .set(Tables.USERS.LARGEST_LENGTH, report.getLargestLength())
                        .set(Tables.USERS.NUM_FISH_CAUGHT, report.getNumFishCaught())
                        .set(Tables.USERS.TOTAL_FISH_LENGTH, report.getTotalFishLength())
                        .set(Tables.USERS.COMPETITIONS_WON, report.getCompetitionsWon())
                        .set(Tables.USERS.COMPETITIONS_JOINED, report.getCompetitionsJoined())
                        .where(Tables.USERS.UUID.eq(uuid.toString()))
                        .execute();

                DatabaseUtil.writeDbVerbose("Written user report for (%s) to the database.".formatted(uuid));
                return rowsUpdated;
            }
        }.executeUpdate();
    }

    @Override
    public UserReport readUserReport(@NotNull UUID uuid) {
        return new ExecuteQuery<UserReport>(connectionFactory, settings) {
            @Override
            protected UserReport onRunQuery(DSLContext dslContext) throws Exception {
                org.jooq.Record tableRecord = dslContext.select()
                        .from(Tables.USERS)
                        .where(Tables.USERS.UUID.eq(uuid.toString()))
                        .fetchOne();
                if (tableRecord == null)
                    return empty();

                DatabaseUtil.writeDbVerbose("Read user report for user (%s)".formatted(uuid.toString()));
                final int id = tableRecord.getValue(Tables.USERS.ID);
                final int numFishCaught = tableRecord.getValue(Tables.USERS.NUM_FISH_CAUGHT);
                final int competitionsWon = tableRecord.getValue(Tables.USERS.COMPETITIONS_WON);
                final int competitionsJoined = tableRecord.getValue(Tables.USERS.COMPETITIONS_JOINED);
                final String firstFish = tableRecord.getValue(Tables.USERS.FIRST_FISH);
                final String lastFish = tableRecord.getValue(Tables.USERS.LAST_FISH);
                final String largestFish = tableRecord.getValue(Tables.USERS.LARGEST_FISH);
                final float totalFishLength = tableRecord.getValue(Tables.USERS.TOTAL_FISH_LENGTH);
                final float largestLength = tableRecord.getValue(Tables.USERS.LARGEST_LENGTH);
                final String uuid = tableRecord.getValue(Tables.USERS.UUID);
                final int fishSold = tableRecord.getValue(Tables.USERS.FISH_SOLD);
                final double moneyEarned = tableRecord.getValue(Tables.USERS.MONEY_EARNED);

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
        return new ExecuteQuery<Boolean>(connectionFactory, settings) {
            @Override
            protected Boolean onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.select()
                        .from(Tables.FISH)
                        .where(Tables.FISH.FISH_NAME.eq(fish.getName())
                                .and(Tables.FISH.FISH_RARITY.eq(fish.getRarity().getValue())))
                        .limit(1)
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
    public void createFishData(@NotNull Fish fish, @NotNull UUID uuid) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.FISH)
                        .set(Tables.FISH.FISH_NAME, fish.getName())
                        .set(Tables.FISH.FISH_RARITY, fish.getRarity().getValue())
                        .set(Tables.FISH.FIRST_FISHER, uuid.toString())
                        .set(Tables.FISH.TOTAL_CAUGHT, 1)
                        .set(Tables.FISH.LARGEST_FISH, Math.round(fish.getLength() * 10f) / 10f)
                        .set(Tables.FISH.FIRST_FISHER, uuid.toString())
                        .set(Tables.FISH.LARGEST_FISHER, uuid.toString())
                        .set(Tables.FISH.FIRST_CATCH_TIME, LocalDateTime.now())
                        .execute();
            }
        }.executeUpdate();
    }

    @Override
    public void incrementFish(@NotNull Fish fish) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.update(Tables.FISH)
                        .set(Tables.FISH.TOTAL_CAUGHT, Tables.FISH.TOTAL_CAUGHT.plus(1))
                        .where(Tables.FISH.FISH_RARITY.eq(fish.getRarity().getValue())
                                .and(Tables.FISH.FISH_NAME.eq(fish.getName())))
                        .execute();
            }
        }.executeUpdate();
    }

    @Override
    public float getLargestFishSize(@NotNull Fish fish) {
        return new ExecuteQuery<Float>(connectionFactory, settings) {
            @Override
            protected Float onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.select()
                        .from(Tables.FISH)
                        .where(Tables.FISH.FISH_RARITY.eq(fish.getRarity().getValue())
                                .and(Tables.FISH.FISH_NAME.eq(fish.getName())))
                        .fetchOne(Tables.FISH.LARGEST_FISH);
            }

            @Override
            protected Float empty() {
                return null;
            }
        }.prepareAndRunQuery();
    }

    @Override
    public void updateLargestFish(@NotNull Fish fish, @NotNull UUID uuid) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.update(Tables.FISH)
                        .set(Tables.FISH.LARGEST_FISH, Math.round(fish.getLength() * 10f) / 10f /* todo use decimal format */)
                        .set(Tables.FISH.LARGEST_FISHER, uuid.toString())
                        .where(Tables.FISH.FISH_RARITY.eq(fish.getRarity().getValue()).and(Tables.FISH.FISH_NAME.eq(fish.getName())))
                        .execute();

            }
        }.executeUpdate();
    }

    @Override
    public List<FishReport> getFishReportsForPlayer(@NotNull UUID uuid) {
        final int userId = getUserId(uuid);

        return new ExecuteQuery<List<FishReport>>(connectionFactory, settings) {
            @Override
            protected List<FishReport> onRunQuery(DSLContext dslContext) throws Exception {
                Result<Record> result = dslContext.select()
                        .from(Tables.FISH_LOG)
                        .where(Tables.FISH_LOG.ID.eq(userId))
                        .fetch();

                if (result.isEmpty()) {
                    return empty();
                }

                DatabaseUtil.writeDbVerbose("Read fish reports for (%s) from the database.".formatted(uuid));
                List<FishReport> reports = new ArrayList<>();
                for (Record recordResult : result) {
                    final String rarity = recordResult.getValue(Tables.FISH_LOG.RARITY);
                    final String fish = recordResult.getValue(Tables.FISH_LOG.FISH);
                    final float largestLength = recordResult.getValue(Tables.FISH_LOG.LARGEST_LENGTH);
                    final int quantity = recordResult.getValue(Tables.FISH_LOG.QUANTITY);
                    final LocalDateTime firstCatchTime = recordResult.getValue(Tables.FISH_LOG.FIRST_CATCH_TIME); // convert to long, or maybe store as long?
                    reports.add(new FishReport(rarity, fish, largestLength, quantity, firstCatchTime));
                }
                return reports;
            }

            @Override
            protected List<FishReport> empty() {
                return List.of();
            }
        }.prepareAndRunQuery();
    }

    @Override
    public List<FishReport> getReportsForFish(@NotNull UUID uuid, @NotNull Fish fish) {
        final int userId = getUserId(uuid);

        return new ExecuteQuery<List<FishReport>>(connectionFactory, settings) {
            @Override
            protected List<FishReport> onRunQuery(DSLContext dslContext) throws Exception {
                Result<Record> result = dslContext.select()
                        .from(Tables.FISH_LOG)
                        .where(Tables.FISH_LOG.ID.eq(userId)
                                .and(Tables.FISH_LOG.FISH.eq(fish.getName()))
                                .and(Tables.FISH_LOG.RARITY.eq(fish.getRarity().getValue())))
                        .fetch();

                if (result.isEmpty()) {
                    return empty();
                }

                DatabaseUtil.writeDbVerbose("Read fish reports for (%s) from the database.".formatted(uuid));
                List<FishReport> reports = new ArrayList<>();
                for (Record recordResult : result) {
                    final String rarity = recordResult.getValue(Tables.FISH_LOG.RARITY);
                    final String fish = recordResult.getValue(Tables.FISH_LOG.FISH);
                    final float largestLength = recordResult.getValue(Tables.FISH_LOG.LARGEST_LENGTH);
                    final int quantity = recordResult.getValue(Tables.FISH_LOG.QUANTITY);
                    final LocalDateTime firstCatchTime = recordResult.getValue(Tables.FISH_LOG.FIRST_CATCH_TIME); // convert to long, or maybe store as long?
                    reports.add(new FishReport(rarity, fish, largestLength, quantity, firstCatchTime));
                }
                return reports;
            }

            @Override
            protected List<FishReport> empty() {
                return List.of();
            }
        }.prepareAndRunQuery();
    }

    @Override
    public void addUserFish(@NotNull FishReport report, int userId) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.FISH_LOG)
                        .set(Tables.FISH_LOG.USER_ID, userId)
                        .set(Tables.FISH_LOG.RARITY, report.getRarity())
                        .set(Tables.FISH_LOG.FISH, report.getName())
                        .set(Tables.FISH_LOG.QUANTITY, report.getNumCaught())
                        .set(Tables.FISH_LOG.FIRST_CATCH_TIME, report.getLocalDateTime()) //convert to bytes
                        .set(Tables.FISH_LOG.LARGEST_LENGTH, report.getLargestLength())
                        .execute();
            }
        }.executeUpdate();
    }

    @Override
    public void updateUserFish(@NotNull FishReport report, int userId) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.update(Tables.FISH_LOG)
                        .set(Tables.FISH_LOG.QUANTITY, report.getNumCaught())
                        .set(Tables.FISH_LOG.LARGEST_LENGTH, report.getLargestLength())
                        .where(Tables.FISH_LOG.USER_ID.eq(userId)
                                .and(Tables.FISH_LOG.RARITY.eq(report.getRarity()))
                                .and(Tables.FISH_LOG.FISH.eq(report.getName())))
                        .execute();
            }
        }.executeUpdate();

    }

    @Override
    public void writeFishReports(@NotNull UUID uuid, @NotNull List<FishReport> reports) {
        int userID = getUserId(uuid);
        for (FishReport report : reports) {
            if (userHasFish(report.getRarity(), report.getName(), userID)) {
                updateUserFish(report, userID);
            } else {
                addUserFish(report, userID);
            }
        }
    }

    @Override
    public boolean userHasFish(@NotNull String rarity, @NotNull String fish, int id) {
        return new ExecuteQuery<Boolean>(connectionFactory, settings) {
            @Override
            protected Boolean onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.select()
                        .from(Tables.FISH_LOG)
                        .where(Tables.FISH_LOG.ID.eq(id)
                                .and(Tables.FISH_LOG.RARITY.eq(rarity)
                                        .and(Tables.FISH_LOG.FISH.eq(fish))))
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
    public void createCompetitionReport(@NotNull Competition competition) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                final Leaderboard leaderboard = competition.getLeaderboard();
                InsertSetMoreStep<CompetitionsRecord> common = dslContext.insertInto(Tables.COMPETITIONS)
                        .set(Tables.COMPETITIONS.COMPETITION_NAME, competition.getCompetitionName());

                if (leaderboard.getSize() <= 0) {
                    return common.set(Tables.COMPETITIONS.WINNER_UUID, leaderboard.getTopEntry().getPlayer().toString())
                            .set(Tables.COMPETITIONS.WINNER_FISH, prepareRarityFishString(leaderboard.getEntry(0).getFish()))
                            .set(Tables.COMPETITIONS.WINNER_SCORE, leaderboard.getTopEntry().getValue())
                            .set(Tables.COMPETITIONS.CONTESTANTS, prepareContestantsString(leaderboard.getEntries()))
                            .execute();
                }

                return common.set(Tables.COMPETITIONS.WINNER_UUID, "None")
                        .set(Tables.COMPETITIONS.WINNER_FISH, "None")
                        .set(Tables.COMPETITIONS.WINNER_SCORE, 0f)
                        .set(Tables.COMPETITIONS.CONTESTANTS, "None")
                        .execute();
            }
        }.executeUpdate();
    }

    private String prepareContestantsString(@NotNull List<CompetitionEntry> entries) {
        return StringUtils.join(entries.stream().map(CompetitionEntry::getPlayer).toList(), ",");
    }

    private @NotNull String prepareRarityFishString(final @NotNull Fish fish) {
        return fish.getRarity().getValue() + ":" + fish.getName();
    }

    @Override
    public void createSale(@NotNull String transactionId, @NotNull String fishName, @NotNull String fishRarity, int fishAmount, double fishLength, double priceSold) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.USERS_SALES)
                        .set(Tables.USERS_SALES.TRANSACTION_ID, transactionId)
                        .set(Tables.USERS_SALES.FISH_NAME, fishName)
                        .set(Tables.USERS_SALES.FISH_RARITY, fishRarity)
                        .set(Tables.USERS_SALES.FISH_AMOUNT, fishAmount)
                        .set(Tables.USERS_SALES.FISH_LENGTH, fishLength)
                        .set(Tables.USERS_SALES.PRICE_SOLD, Math.round(priceSold * 10) / 10.0)
                        .execute();
            }
        }.executeUpdate();

    }

    @Override
    public void createTransaction(@NotNull String transactionId, int userId, @NotNull Timestamp timestamp) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.TRANSACTIONS)
                        .set(Tables.TRANSACTIONS.ID, transactionId)
                        .set(Tables.TRANSACTIONS.USER_ID, userId)
                        .set(Tables.TRANSACTIONS.TIMESTAMP, timestamp.toLocalDateTime())
                        .execute();
            }
        }.executeUpdate();
    }

    public void shutdown() {
        try {
            this.connectionFactory.shutdown();
        } catch (Exception e) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
