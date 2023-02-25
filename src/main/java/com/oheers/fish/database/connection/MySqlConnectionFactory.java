package com.oheers.fish.database.connection;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author sarhatabaot
 */
public class MySqlConnectionFactory extends ConnectionFactory{
    @Override
    protected void configureDatabase(@NotNull HikariConfig config, String address, int port, String databaseName, String username, String password) {
        config.setJdbcUrl("jdbc:mysql://"+address+":"+port+"/"+databaseName);
        config.setUsername(username);
        config.setPassword(password);
    }
    
    @Override
    protected void overrideProperties(@NotNull Map<String, String> properties) {
        properties.putIfAbsent("cachePrepStmts", "true");
        properties.putIfAbsent("prepStmtCacheSize", "250");
        properties.putIfAbsent("prepStmtCacheSqlLimit", "2048");
        properties.putIfAbsent("useServerPrepStmts", "true");
        properties.putIfAbsent("useLocalSessionState", "true");
        properties.putIfAbsent("rewriteBatchedStatements", "true");
        properties.putIfAbsent("cacheResultSetMetadata", "true");
        properties.putIfAbsent("cacheServerConfiguration", "true");
        properties.putIfAbsent("elideSetAutoCommits", "true");
        properties.putIfAbsent("maintainTimeStats", "false");
        properties.putIfAbsent("alwaysSendSetIsolation", "false");
        properties.putIfAbsent("cacheCallableStmts", "true");
    
        // https://stackoverflow.com/a/54256150
        properties.putIfAbsent("serverTimezone", "UTC");
    
        super.overrideProperties(properties);
    }
}
