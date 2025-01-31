CREATE TABLE `${table.prefix}competitions` (
   id INTEGER NOT NULL,
   competition_name VARCHAR(256) NOT NULL,
   winner_uuid VARCHAR(128) NOT NULL,
   winner_fish VARCHAR(256) NOT NULL,
   winner_score REAL NOT NULL,
   contestants VARCHAR(15000) NOT NULL,
   PRIMARY KEY (id AUTOINCREMENT)
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
   id INTEGER NOT NULL,
   uuid VARCHAR(128) NOT NULL,
   first_fish VARCHAR(256) NOT NULL,
   last_fish VARCHAR(256) NOT NULL,
   largest_fish VARCHAR(256) NOT NULL,
   largest_length REAL NOT NULL,
   num_fish_caught VARCHAR(256) NOT NULL,
   total_fish_length REAL NOT NULL,
   competitions_won INT NOT NULL,
   competitions_joined INT NOT NULL,
   PRIMARY KEY (id AUTOINCREMENT)
);