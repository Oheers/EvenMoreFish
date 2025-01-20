package com.oheers.fish.database.connection;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.migrate.LegacyToV3DatabaseMigration;
import com.oheers.fish.utils.ManifestUtil;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.command.CommandSender;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.MappedTable;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationManager {
    private final Logger logger = LoggerFactory.getLogger(MigrationManager.class.getName());
    private final FluentConfiguration baseFlywayConfiguration;
    private final Flyway defaultFlyway;
    private final ConnectionFactory connectionFactory;

    public MigrationManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.baseFlywayConfiguration = getBaseFlywayConfiguration(connectionFactory);
        this.defaultFlyway = this.baseFlywayConfiguration.load();
    }

    public void migrateFromVersion(String currentDbVersion, boolean baseline) {
        final String baselineVersion = ManifestUtil.getAttributeFromManifest("Database-Baseline-Version", "7.0");
        Flyway migrate = this.baseFlywayConfiguration
                .baselineVersion(baselineVersion)
                .load();

        try {
            if (shouldBaseline(baseline, baselineVersion, currentDbVersion)) {
                migrate.baseline();
            }

            migrate.migrate();
        } catch (FlywayException e) {
            logger.error("Database migration failed for version: {}", currentDbVersion, e);
        }
    }

    private boolean shouldBaseline(final boolean baseline, final String baselineVersion, final String currentDbVersion) {
        if (!baseline)
            return false;

        ComparableVersion version1 = new ComparableVersion(baselineVersion); // e.g. 7.0
        ComparableVersion version2 = new ComparableVersion(currentDbVersion); // e.g. 7.1

        int compare = version1.compareTo(version2);
        return compare == 0;
    }

    public boolean usingV2() {
        boolean dataFolder = Files.isDirectory(Paths.get(EvenMoreFish.getInstance().getDataFolder() + "/data/"));
        return dataFolder || queryTableExistence("Fish2");
    }

    public boolean queryTableExistence(final String tableName) {
        final Settings settings = new Settings();
        settings.setExecuteLogging(true);
        settings.withRenderMapping(
                new RenderMapping().withSchemata(
                        new MappedSchema().withInput("")
                                .withOutput(MainConfig.getInstance().getDatabase())
                                .withTables(
                                        new MappedTable()
                                                .withInputExpression(Pattern.compile("\\$\\{table.prefix}(.*)"))
                                                .withOutput(MainConfig.getInstance().getPrefix() + "$1"
                                                )
                                )

                )
        );
        try {
            DSLContext dsl = DSL.using(connectionFactory.getConnection(), settings);

            return dsl.fetchExists(
                    DSL.select()
                            .from("information_schema.tables")
                            .where(DSL.field("table_name").eq(tableName))
                            .and(DSL.noCondition())
            );
        } catch (SQLException | DataAccessException e) {
            return false;
        }
    }

    public void dropFlywaySchemaHistory() {
        try (Connection connection = defaultFlyway.getConfiguration().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement("DROP TABLE IF EXISTS flyway_schema_history")) {
            statement.execute();
            logger.info("Dropped flyway schema history table.");
        } catch (SQLException e) {
            logger.error("Failed to drop Flyway schema history table", e);
        }
    }

    public void repairFlyway() {
        this.baseFlywayConfiguration.load().repair();
    }

    public void cleanFlyway() {
        this.baseFlywayConfiguration.load().clean();
    }

    public MigrationVersion getDatabaseVersion() {
        MigrationInfoService infoService = baseFlywayConfiguration.load().info();
        if (infoService.current() == null) {
            return MigrationVersion.fromVersion("7.0");
        }
        return baseFlywayConfiguration.load().info().current().getVersion();
    }

    public void migrateFromV6ToLatest() {
        Flyway flyway = baseFlywayConfiguration
                .baselineVersion("6")
                .load();

        try {
            flyway.migrate();
        } catch (FlywayException e) {
            logger.error("There was a problem migrating to the latest database version. You may experience issues.", e);
        }
    }

    public void legacyFlywayBaseline() {
        Flyway flyway = baseFlywayConfiguration
                .target("3.1")
                .load();

        flyway.migrate();
    }

    public void legacyInitVersion() {
        Flyway flyway = baseFlywayConfiguration
                .target("3.0")
                .load();

        flyway.migrate();
    }

    /*
    We may use .ignoreMigrationPatterns("versioned:missing") to ignore the missing migrations 3.0, 3.1 (FlywayTeams)
    Instead we use baselineVersion 5, which assumes you were running experimental-features: true before using this version.
    This is caused since we added some initial migrations that weren't there prior to 1.7.0 #67
     */
    public void migrateFromV5ToLatest() {
        Flyway flyway = baseFlywayConfiguration
                .baselineVersion("5")
                .load();


        try {
            dropFlywaySchemaHistory();

            flyway.migrate();
        } catch (FlywayException e) {
            logger.error("There was a problem migrating to the latest database version. You may experience issues.", e);
        }
    }

    @Contract(pure = true)
    private @NotNull String getMigrationLocation(final String dialect) {
        return "db/migrations/" + dialect;
    }

    /**
     * Converts a V2 database system to a V3 database system. The server must not crash during this process as this may
     * lead to data loss, but honestly I'm not 100% sure on that one. Data is read from the /data/ folder and is
     * inserted into the new database system then the /data/ folder is renamed to /data-old/.
     *
     * @param initiator The person who started the migration.
     */
    public void migrateLegacy(CommandSender initiator) {
        LegacyToV3DatabaseMigration legacy = new LegacyToV3DatabaseMigration(EvenMoreFish.getInstance().getDatabase(), this);
        legacy.migrate(initiator);
    }

    private FluentConfiguration getBaseFlywayConfiguration(ConnectionFactory connectionFactory) {

        return Flyway.configure(getClass().getClassLoader())
                .dataSource(connectionFactory.dataSource)
                .placeholders(Map.of(
                        "table.prefix", MainConfig.getInstance().getPrefix()
                ))
                .locations(getMigrationLocation(MainConfig.getInstance().getDatabaseType()))
                .validateMigrationNaming(true)
                .createSchemas(true)
                .baselineOnMigrate(true)
                .table(MainConfig.getInstance().getPrefix() + "flyway_schema_history");
    }
}
