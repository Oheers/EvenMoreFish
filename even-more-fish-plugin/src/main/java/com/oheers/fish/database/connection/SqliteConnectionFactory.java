package com.oheers.fish.database.connection;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SqliteConnectionFactory extends ConnectionFactory {
    @Override
    protected void configureDatabase(@NotNull HikariConfig config, String address, int port, String databaseName, String username, String password) {
        config.setJdbcUrl("jdbc:sqlite:plugins/EvenMoreFish/" + databaseName + ".db");
//        config.setMaximumPoolSize(1); // SQLite doesn't benefit from multiple connections
    }

    @Override
    protected void overrideProperties(@NotNull Map<String, String> properties) {
        properties.putIfAbsent("cachePrepStmts", "true");
        properties.putIfAbsent("prepStmtCacheSize", "250");
        properties.putIfAbsent("prepStmtCacheSqlLimit", "2048");

        super.overrideProperties(properties);
    }

    @Override
    public String getType() {
        return "SQLITE";
    }
}
