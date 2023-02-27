package com.oheers.fish.database.migrate.migrations;


import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;

/**
 * @author sarhatabaot
 */
public class V3_1_CreateUsersSalesTableAndTransactionsTable extends BaseJavaMigration {
    //TODO remember to account for sqlite
    @Override
    public void migrate(Context context) throws Exception {
        try(PreparedStatement statement = context.getConnection().prepareStatement(
            "CREATE TABLE emf_transactions ( " +
                "id VARCHAR(22) NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "timestamp TIMESTAMP NOT NULL, " +
                "PRIMARY KEY(id)" +
            ");")) {
            statement.execute();
        }
        
        try(PreparedStatement statement = context.getConnection().prepareStatement(
            "CREATE TABLE emf_users_sales ( " +
                "id INTEGER NOT NULL AUTO_INCREMENT, " +
                "transaction_id VARCHAR(22) NOT NULL, " + //todo
                "fish_name VARCHAR NOT NULL," +
                "fish_amount INTEGER NOT NULL, " +
                "price_sold FLOAT NOT NULL, " +
                "PRIMARY KEY(id)" +
                ");"
        )) {
            statement.execute();
        }
    }
}
