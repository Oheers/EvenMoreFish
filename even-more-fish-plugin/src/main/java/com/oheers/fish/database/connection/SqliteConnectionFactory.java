package com.oheers.fish.database.connection;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

public class SqliteConnectionFactory extends ConnectionFactory {
    @Override
    protected void configureDatabase(@NotNull HikariConfig config, String address, int port, String databaseName, String username, String password) {
        config.setJdbcUrl("jdbc:sqlite:plugins/EvenMoreFish/" + databaseName + ".db");
    }

    @Override
    public String getType() {
        return "SQLITE";
    }
}
