package com.oheers.fish.database;

import org.jetbrains.annotations.NotNull;

public enum Table {

	EMF_COMPETITIONS("emf_competitions",
			"CREATE TABLE emf_competitions (\n" +
					"	 id INTEGER NOT NULL, \n" +
					" 	 competition_name VARCHAR(256) NOT NULL, \n" +
					"    winner_uuid VARCHAR(128) NOT NULL, \n" +
					"    winner_fish VARCHAR(256) NOT NULL, \n" +
					"    winner_score INT NOT NULL, \n" +
					"    contestants VARCHAR(15000) NOT NULL, \n" +
					"    PRIMARY KEY(id AUTOINCREMENT) \n" +
					");"
			),
	EMF_FISH("emf_fish",
			"CREATE TABLE emf_fish (\n" +
					"    fish_name VARCHAR(100) NOT NULL,\n" +
					"    fish_rarity VARCHAR(100) NOT NULL,\n" +
					"    first_fisher VARCHAR(36) NOT NULL, \n" +
					"    total_caught INTEGER NOT NULL,\n" +
					"    largest_fish REAL NOT NULL,\n" +
					"    largest_fisher VARCHAR(36) NOT NULL,\n" +
					"    first_catch_time LONGBLOB NOT NULL\n" +
					");"
			),
	EMF_FISH_LOG("emf_fish_log",
			"CREATE TABLE emf_fish_log (\n" +
					"    id INT NOT NULL,\n" +
					"    rarity VARCHAR(128) NOT NULL,\n" +
					"    fish VARCHAR(128) NOT NULL, \n" +
					"    quantity INT NOT NULL,\n" +
					"    first_catch_time LONGBLOB NOT NULL,\n" +
					"    largest_length REAL NOT NULL\n" +
					");"
	),
	EMF_USERS("emf_users",
			"CREATE TABLE emf_users (\n" +
			"	 id INTEGER NOT NULL, \n" +
			"    uuid VARCHAR(128) NOT NULL, \n" +
			"	 first_fish VARCHAR(256) NOT NULL, \n" +
			" 	 last_fish VARCHAR(256) NOT NULL, \n" +
			"    largest_fish VARCHAR(256) NOT NULL, \n" +
			"    num_fish_caught VARCHAR(256) NOT NULL, \n" +
			"    total_fish_length INT NOT NULL, \n" +
			"    competitions_won INT NOT NULL, \n" +
			"    competitions_joined INT NOT NULL,\n" +
			"    PRIMARY KEY(id AUTOINCREMENT) \n" +
			");");

	public final String tableID;
	public final String creationCode;

	Table(@NotNull final String tableID, final String creationCode) {
		this.tableID = tableID;
		this.creationCode = creationCode;
	}

	public String getTableID() {
		return tableID;
	}

	public String getCreationCode() {
		return creationCode;
	}
}
