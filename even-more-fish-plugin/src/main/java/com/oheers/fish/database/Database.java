package com.oheers.fish.database;


import org.jooq.conf.*;

import java.util.regex.Pattern;

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
                                        new MappedTable()
                                                .withInputExpression(Pattern.compile("\\$\\{table.prefix}_(.*)"))
                                                .withOutput(tablePrefix + "$1"
                                                )
                                        )

                )
        );
    }
}
