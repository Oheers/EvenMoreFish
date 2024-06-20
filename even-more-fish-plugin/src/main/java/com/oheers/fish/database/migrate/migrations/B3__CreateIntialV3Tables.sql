CREATE TABLE `${table.prefix}competitions` (
   id INTEGER NOT NULL${auto.increment},
   competition_name VARCHAR(256) NOT NULL,
   winner_uuid VARCHAR(128) NOT NULL,
   winner_fish VARCHAR(256) NOT NULL,
   winner_score REAL NOT NULL,
   contestants VARCHAR(15000) NOT NULL,
   ${primary.key}
);

CREATE TABLE `${table.prefix}competitions` (
   id INTEGER NOT NULL${auto.increment},
   competition_name VARCHAR(256) NOT NULL,
   winner_uuid VARCHAR(128) NOT NULL,
   winner_fish VARCHAR(256) NOT NULL,
   winner_score REAL NOT NULL,
   contestants VARCHAR(15000) NOT NULL,
   ${primary.key}
);


CREATE TABLE `${table.prefix}fish` (
   fish_name VARCHAR(100) NOT NULL,
   fish_rarity VARCHAR(100) NOT NULL,
   first_fisher VARCHAR(36) NOT NULL,
   total_caught INTEGER NOT NULL,
   largest_fish REAL NOT NULL,
   largest_fisher VARCHAR(36) NOT NULL,
   first_catch_time LONGBLOB NOT NULL
);

CREATE TABLE `${table.prefix}fish_log` (
   id INT NOT NULL,
   rarity VARCHAR(128) NOT NULL,
   fish VARCHAR(128) NOT NULL,
   quantity INT NOT NULL,
   first_catch_time LONGBLOB NOT NULL,
   largest_length REAL NOT NULL
);

CREATE TABLE `${table.prefix}users` (
   id INTEGER NOT NULL ${auto.increment},
   uuid VARCHAR(128) NOT NULL,
   first_fish VARCHAR(256) NOT NULL,
   last_fish VARCHAR(256) NOT NULL,
   argest_fish VARCHAR(256) NOT NULL,
   largest_length REAL NOT NULL,
   num_fish_caught VARCHAR(256) NOT NULL,
   total_fish_length REAL NOT NULL,
   competitions_won INT NOT NULL,
   competitions_joined INT NOT NULL,
   ${primary.key}
);