package com.oheers.fish.database;

import com.oheers.fish.config.MainConfig;
import org.jetbrains.annotations.NotNull;

public enum Table {

    EMF_COMPETITIONS("competitions",
            "CREATE TABLE emf_competitions (\n" +
                    "	 id INTEGER NOT NULL, \n" +
                    " 	 competition_name VARCHAR(256) NOT NULL, \n" +
                    "    winner_uuid VARCHAR(128) NOT NULL, \n" +
                    "    winner_fish VARCHAR(256) NOT NULL, \n" +
                    "    winner_score REAL NOT NULL, \n" +
                    "    contestants VARCHAR(15000) NOT NULL, \n" +
                    "    PRIMARY KEY(id AUTOINCREMENT) \n" +
                    ");", false
    ),

    EMF_COMPETITIONS_MYSQL("competitions",
            "CREATE TABLE emf_competitions (\n" +
                    "	 id INTEGER NOT NULL AUTO_INCREMENT, \n" +
                    " 	 competition_name VARCHAR(256) NOT NULL, \n" +
                    "    winner_uuid VARCHAR(128) NOT NULL, \n" +
                    "    winner_fish VARCHAR(256) NOT NULL, \n" +
                    "    winner_score REAL NOT NULL, \n" +
                    "    contestants VARCHAR(15000) NOT NULL, \n" +
                    "    PRIMARY KEY(id) \n" +
                    ");", true
    ),
    EMF_FISH("fish",
            "CREATE TABLE emf_fish (\n" +
                    "    fish_name VARCHAR(100) NOT NULL,\n" +
                    "    fish_rarity VARCHAR(100) NOT NULL,\n" +
                    "    first_fisher VARCHAR(36) NOT NULL, \n" +
                    "    total_caught INTEGER NOT NULL,\n" +
                    "    largest_fish REAL NOT NULL,\n" +
                    "    largest_fisher VARCHAR(36) NOT NULL,\n" +
                    "    first_catch_time LONGBLOB NOT NULL\n" +
                    ");", true
    ),
    EMF_FISH_LOG("fish_log",
            "CREATE TABLE emf_fish_log (\n" +
                    "    id INT NOT NULL,\n" +
                    "    rarity VARCHAR(128) NOT NULL,\n" +
                    "    fish VARCHAR(128) NOT NULL, \n" +
                    "    quantity INT NOT NULL,\n" +
                    "    first_catch_time LONGBLOB NOT NULL,\n" +
                    "    largest_length REAL NOT NULL\n" +
                    ");", true
    ),
    EMF_USERS("users",
            "CREATE TABLE emf_users (\n" +
                    "	 id INTEGER NOT NULL, \n" +
                    "    uuid VARCHAR(128) NOT NULL, \n" +
                    "	 first_fish VARCHAR(256) NOT NULL, \n" +
                    " 	 last_fish VARCHAR(256) NOT NULL, \n" +
                    "    largest_fish VARCHAR(256) NOT NULL, \n" +
                    "    largest_length REAL NOT NULL, \n" +
                    "    num_fish_caught VARCHAR(256) NOT NULL, \n" +
                    "    total_fish_length REAL NOT NULL, \n" +
                    "    competitions_won INT NOT NULL, \n" +
                    "    competitions_joined INT NOT NULL,\n" +
                    "    PRIMARY KEY(id AUTOINCREMENT) \n" +
                    ");", false),

    EMF_USERS_MYSQL("users",
            "CREATE TABLE emf_users (\n" +
                    "	 id INTEGER NOT NULL AUTO_INCREMENT, \n" +
                    "    uuid VARCHAR(128) NOT NULL, \n" +
                    "	 first_fish VARCHAR(256) NOT NULL, \n" +
                    " 	 last_fish VARCHAR(256) NOT NULL, \n" +
                    "    largest_fish VARCHAR(256) NOT NULL, \n" +
                    "    largest_length REAL NOT NULL, \n" +
                    "    num_fish_caught VARCHAR(256) NOT NULL, \n" +
                    "    total_fish_length REAL NOT NULL, \n" +
                    "    competitions_won INT NOT NULL, \n" +
                    "    competitions_joined INT NOT NULL,\n" +
                    "    PRIMARY KEY(id) \n" +
                    ");", true
    );

    public final String tableID;
    public final String creationCode;
    public final boolean isMySQLCompatible;

    Table(@NotNull final String tableID, final String creationCode, final boolean mySQLCompatible) {
        this.tableID = MainConfig.getInstance().getPrefix() + tableID;
        this.creationCode = creationCode;
        this.isMySQLCompatible = mySQLCompatible;
    }

    public String getTableID() {
        return tableID;
    }

    public String getCreationCode() {
        return creationCode;
    }

    public boolean isMySQLCompatible() {
        return isMySQLCompatible;
    }
}
