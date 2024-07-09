package com.oheers.fish.database.connection;


import com.google.common.collect.ImmutableMap;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.DatabaseUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
 * We can add additional factories to allow for multiple database support in the future.
 */
public abstract class ConnectionFactory {
    protected HikariDataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);


    /**
     * This may be different with every database type.
     *
     * @param config       hikari config
     * @param address      address
     * @param port         port
     * @param databaseName databaseName
     * @param username     username
     * @param password     password
     */
    protected abstract void configureDatabase(HikariConfig config, String address, int port, String databaseName, String username, String password);

    private String getDatabaseAddress() {
        return MainConfig.getInstance().getAddress().split(":")[0];
    }

    private int getDatabasePort() {
        if (!MainConfig.getInstance().getAddress().contains(":")) {
            return 3306;
        }
        try {
            return Integer.parseInt(MainConfig.getInstance().getAddress().split(":")[1]);
        } catch (NumberFormatException e) {
            return 3306;
        }
    }

    public void init() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("evenmorefish-hikari");

        configureDatabase(config, getDatabaseAddress(), getDatabasePort(), MainConfig.getInstance().getDatabase(), MainConfig.getInstance().getUsername(), MainConfig.getInstance().getPassword());
        config.setInitializationFailTimeout(-1);

        Map<String, String> properties = new HashMap<>();

        overrideProperties(properties);
        setProperties(config, properties);

        this.dataSource = new HikariDataSource(config);
        logger.info("Connected to database!");
    }

    public void flyway6toLatest() {
        Flyway flyway = getBaseFlywayConfiguration()
                .baselineVersion("6")
                .load();

        try {
            flyway.migrate();
        } catch (FlywayException e) {
            logger.error("There was a problem migrating to the latest database version. You may experience issues.", e);
        }
    }

    public void legacyFlywayBaseline() {
        Flyway flyway = getBaseFlywayConfiguration()
                .target("3.1")
                .load();

        flyway.migrate();
    }

    public void legacyInitVersion() {
        Flyway flyway = getBaseFlywayConfiguration()
                .target("3.0")
                .load();

        flyway.migrate();
    }

    /*
    We may use .ignoreMigrationPatterns("versioned:missing") to ignore the missing migrations 3.0, 3.1 (FlywayTeams)
    Instead we use baselineVersion 5, which assumes you were running experimental-features: true before using this version.
    This is caused since we added some initial migrations that weren't there prior to 1.7.0 #67
     */
    public void flyway5toLatest() {
        Flyway flyway = getBaseFlywayConfiguration()
                .baselineVersion("5")
                .load();


        try {
            dropFlywaySchemaHistory();

            flyway.migrate();
        } catch (FlywayException e) {
            logger.error("There was a problem migrating to the latest database version. You may experience issues.", e);
        }
    }

    private void dropFlywaySchemaHistory() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement prepStatement = connection.prepareStatement(DatabaseUtil.parseSqlString("DROP TABLE IF EXISTS`${table.prefix}flyway_schema_history`", connection))) {
                prepStatement.execute();
            }
        } catch (SQLException e) {
            logger.error("Failed to drop flyway_schema_history table", e);
        }
    }

    //LP
    protected void overrideProperties(@NotNull Map<String, String> properties) {
        properties.putIfAbsent("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));
    }

    //LP
    protected void setProperties(HikariConfig config, @NotNull Map<String, String> properties) {
        for (Map.Entry<String, String> property : properties.entrySet()) {
            config.addDataSourceProperty(property.getKey(), property.getValue());
        }
    }

    public void shutdown() {
        if (this.dataSource != null) {
            this.dataSource.close();
        }
    }

    public abstract String getType();

    public Connection getConnection() throws SQLException {
        if (this.dataSource == null) {
            throw new SQLException("Null data source");
        }

        Connection connection = this.dataSource.getConnection();
        if (connection == null) {
            throw new SQLException("Null connection");
        }

        return connection;
    }

    private FluentConfiguration getBaseFlywayConfiguration() {
        return Flyway.configure(getClass().getClassLoader())
                .dataSource(dataSource)
                .placeholders(ImmutableMap.of(
                        "table.prefix", MainConfig.getInstance().getPrefix(),
                        "auto.increment", MainConfig.getInstance().getDatabaseType().equalsIgnoreCase("mysql") ? "AUTO_INCREMENT" : "",
                        "primary.key", MainConfig.getInstance().getDatabaseType().equalsIgnoreCase("mysql") ? "PRIMARY KEY (id)" : "PRIMARY KEY (id AUTOINCREMENT)",
                        "v6.alter.columns", !MainConfig.getInstance().getDatabaseType().equalsIgnoreCase("sqlite") ?
                                "ALTER TABLE `${table.prefix}competitions` ALTER COLUMN contestants text;" +
                                        "ALTER TABLE `${table.prefix}fish` ADD PRIMARY KEY (fish_name);" +
                                        "ALTER TABLE `${table.prefix}fish_log` ADD CONSTRAINT FK_FishLog_User FOREIGN KEY(id) REFERENCES `${table.prefix}users(id)`;" +
                                        "ALTER TABLE `${table.prefix}users_sales` ADD CONSTRAINT FK_UsersSales_Transaction FOREIGN KEY (transaction_id) REFERENCES `${table.prefix}transactions(id)`;"
                                : ""
                ))
                .validateMigrationNaming(true)
                .createSchemas(true)
                .baselineOnMigrate(true)
                .table(MainConfig.getInstance().getPrefix() + "flyway_schema_history");
    }

    public MigrationVersion getDatabaseVersion() {
        MigrationInfoService infoService = getBaseFlywayConfiguration().load().info();
        if (infoService.current() == null) {
            return MigrationVersion.fromVersion("0");
        }
        return getBaseFlywayConfiguration().load().info().current().getVersion();
    }
}
