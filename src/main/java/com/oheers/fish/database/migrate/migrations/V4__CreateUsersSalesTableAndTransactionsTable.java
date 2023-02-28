package com.oheers.fish.database.migrate.migrations;


import com.oheers.fish.database.DatabaseUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;

/**
 * @author sarhatabaot
 */
public class V4__CreateUsersSalesTableAndTransactionsTable extends BaseJavaMigration {
    
    @Override
    public void migrate(@NotNull Context context) throws Exception {
        String createEmfTransactionSql = "CREATE TABLE emf_transactions ( " +
            "id VARCHAR(22) NOT NULL, " +
            "user_id INTEGER NOT NULL, " +
            "timestamp TIMESTAMP NOT NULL, " +
            "PRIMARY KEY(id)" +
            ");";
        try (PreparedStatement statement = context.getConnection().prepareStatement(
            DatabaseUtil.parseSqlString(createEmfTransactionSql, context.getConnection())
        )) {
            statement.execute();
        }
        String createEmfUserSalesSql = "CREATE TABLE emf_users_sales ( " +
            "id INTEGER NOT NULL${auto.increment}, " +
            "transaction_id VARCHAR(22) NOT NULL, " +
            "fish_name VARCHAR(256) NOT NULL," +
            "fish_rarity VARCHAR(256) NOT NULL," +
            "fish_amount INTEGER NOT NULL, " +
            "price_sold FLOAT NOT NULL, " +
            "${primary.key}" +
            ");";
        try (PreparedStatement statement = context.getConnection().prepareStatement(
            DatabaseUtil.parseSqlString(createEmfUserSalesSql, context.getConnection())
        )) {
            statement.execute();
        }
    }
}
