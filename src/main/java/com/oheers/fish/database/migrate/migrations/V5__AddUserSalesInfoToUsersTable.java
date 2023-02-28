package com.oheers.fish.database.migrate.migrations;


import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;

public class V5__AddUserSalesInfoToUsersTable extends BaseJavaMigration {
    /*
    We should add a total sold column, and total money earned from selling.
     */
    @Override
    public void migrate(Context context) throws Exception {
        String sql = "ALTER TABLE emf_users" +
            "ADD fish_sold INTEGER DEFAULT 0;" +
            "ADD money_earned DOUBLE DEFAULT 0;";
        try (PreparedStatement statement = context.getConnection().prepareStatement(sql)){
            statement.execute();
        }
    }
}
