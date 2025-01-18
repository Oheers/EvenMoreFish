package com.oheers.fish.database.connection;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class H2ConnectionFactory extends ConnectionFactory {
    @Override
    protected void configureDatabase(@NotNull HikariConfig config, String address, int port, String databaseName, String username, String password) {
        config.setJdbcUrl("jdbc:h2:plugins/EvenMoreFish/" + databaseName + ".mv.db");
        config.setUsername(username);
        config.setPassword(password);
    }

    @Override
    protected void overrideProperties(@NotNull Map<String, String> properties) {
        properties.putIfAbsent("MODE", "MySQL"); // Sets compatibility mode to MySQL
        properties.putIfAbsent("DATABASE_TO_UPPER", "false"); // Keeps table names case-sensitive
        properties.putIfAbsent("MV_STORE", "true");
        properties.putIfAbsent("TRACE_LEVEL_FILE", "0"); // Disables trace logs

        super.overrideProperties(properties);
    }

    @Override
    public String getType() {
        return "H2";
    }

    @Override
    public String getDriverClass() {
        return "org.h2.Driver";
    }
}
