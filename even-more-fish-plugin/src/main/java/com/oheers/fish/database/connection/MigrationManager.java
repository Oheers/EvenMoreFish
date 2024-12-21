package com.oheers.fish.database.connection;

import com.oheers.fish.config.MainConfig;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationManager {
    private final Logger logger = LoggerFactory.getLogger(MigrationManager.class.getName());
    private final FluentConfiguration baseFlywayConfiguration;
    private final Flyway defaultFlyway;

    public MigrationManager(ConnectionFactory connectionFactory) {
        this.baseFlywayConfiguration = getBaseFlywayConfiguration(connectionFactory);
        this.defaultFlyway = this.baseFlywayConfiguration.load();
    }

    public void migrateFromVersion(String baselineVersion, boolean baseline) {
        this.baseFlywayConfiguration.baselineVersion(baselineVersion);
        Flyway migrate = this.baseFlywayConfiguration.load();
        try {
            if (baseline) {
                migrate.baseline();
            }

            migrate.migrate();
            logger.info("Database successfully migrated starting from version: {}", baselineVersion);
        } catch (FlywayException e) {
            logger.error("Database migration failed for version: {}", baselineVersion, e);
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

    public MigrationVersion getDatabaseVersion() {
        MigrationInfoService infoService = baseFlywayConfiguration.load().info();
        if (infoService.current() == null) {
            return MigrationVersion.fromVersion("0");
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
        return "src/resources/migrations/" + dialect;
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
