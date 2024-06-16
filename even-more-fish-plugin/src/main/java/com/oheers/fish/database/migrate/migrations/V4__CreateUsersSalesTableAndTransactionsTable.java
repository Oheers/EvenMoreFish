package com.oheers.fish.database.migrate.migrations;


import com.oheers.fish.database.DatabaseUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class V4__CreateUsersSalesTableAndTransactionsTable extends BaseJavaMigration {
    
    @Override
    public void migrate(@NotNull Context context) throws Exception {
        final Connection connection = context.getConnection();
        String createEmfTransactionSql = "CREATE TABLE ${table.prefix}transactions ( " +
            "id VARCHAR(22) NOT NULL, " +
            "user_id INTEGER NOT NULL, " +
            "timestamp TIMESTAMP NOT NULL, " +
            "PRIMARY KEY(id)" +
            ");";
        try (PreparedStatement statement = connection.prepareStatement(
            DatabaseUtil.parseSqlString(createEmfTransactionSql, context.getConnection())
        )) {
            statement.execute();
        }
        String createEmfUserSalesSql = "CREATE TABLE ${table.prefix}users_sales ( " +
            "id INTEGER NOT NULL${auto.increment}, " +
            "transaction_id VARCHAR(22) NOT NULL, " +
            "fish_name VARCHAR(256) NOT NULL," +
            "fish_rarity VARCHAR(256) NOT NULL," +
            "fish_amount INTEGER NOT NULL, " +
            "fish_length DOUBLE NOT NULL, "+
            "price_sold DOUBLE NOT NULL, " +
            "${primary.key}" +
            ");";
        try (PreparedStatement statement = connection.prepareStatement(
            DatabaseUtil.parseSqlString(createEmfUserSalesSql, context.getConnection())
        )) {
            statement.execute();
        }
    }
}
