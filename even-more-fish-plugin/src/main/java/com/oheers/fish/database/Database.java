package com.oheers.fish.database;


import org.jooq.conf.MappedSchema;
import org.jooq.conf.MappedTable;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;

public class Database {
    private Settings jooqSettings;

    public Database() {
        this.jooqSettings = new Settings();
    }

    public void initSettings(final String tablePrefix, final String dbName) {
        jooqSettings.setExecuteLogging(true);
        jooqSettings.withRenderMapping(
                new RenderMapping().withSchemata(
                        new MappedSchema().withInput("")
                                .withOutput(dbName)
                                .withTables(
                                        new MappedTable().withInput("${table.prefix}competitions").withOutput(tablePrefix + "competitions"),
                                        new MappedTable().withInput("${table.prefix}fish").withOutput(tablePrefix + "fish"),
                                        new MappedTable().withInput("${table.prefix}fish_log").withOutput(tablePrefix + "fish_log"),
                                        new MappedTable().withInput("${table.prefix}users").withOutput(tablePrefix + "users"),
                                        new MappedTable().withInput("${table.prefix}users_sales").withOutput(tablePrefix + "users_sales"),
                                        new MappedTable().withInput("${table.prefix}transactions").withOutput(tablePrefix + "transactions")
                                        )
                )
        );
    }
}
