package com.oheers.fish.database;


import com.oheers.fish.config.MainConfig;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtil {
    private DatabaseUtil() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull String determineDbVendor(final @NotNull Connection connection) throws SQLException {
        if (connection.getMetaData().getURL().contains("mysql")) {
            return "mysql";
        }
        //assume it's sqlite otherwise
        return "sqlite";
    }

    public static @NotNull String parseSqlString(final String sql, final Connection connection) throws SQLException {
        final String dbVendor = determineDbVendor(connection);
        if ("mysql".equalsIgnoreCase(dbVendor)) {
            return sql
                    .replace("${table.prefix}", MainConfig.getInstance().getPrefix())
                    .replace("${auto.increment}", " AUTO_INCREMENT")
                    .replace("${primary.key}", "PRIMARY KEY (id)");
        }
        //assume it's sqlite otherwise
        return sql
                .replace("${table.prefix}", MainConfig.getInstance().getPrefix())
                .replace("${auto.increment}", "")
                .replace("${primary.key}", "PRIMARY KEY (id AUTOINCREMENT)");
    }

}
