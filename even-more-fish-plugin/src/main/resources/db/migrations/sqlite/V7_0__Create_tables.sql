CREATE TABLE IF NOT EXISTS `${table.prefix}competitions` (
   id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
   competition_name TEXT NOT NULL,
   winner_uuid TEXT NOT NULL,
   winner_fish TEXT NOT NULL,
   winner_score REAL NOT NULL,
   contestants TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS `${table.prefix}fish` (
   fish_name TEXT NOT NULL PRIMARY KEY,
   fish_rarity TEXT NOT NULL,
   first_fisher TEXT NOT NULL,
   total_caught INTEGER NOT NULL,
   largest_fish REAL NOT NULL,
   largest_fisher TEXT NOT NULL,
   first_catch_time TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS `${table.prefix}fish_log` (
   id INTEGER NOT NULL, -- user id
   rarity TEXT NOT NULL,
   fish TEXT NOT NULL,
   quantity INTEGER NOT NULL,
   first_catch_time TEXT NOT NULL,
   largest_length REAL NOT NULL,
   -- [jooq ignore start]
   FOREIGN KEY (id) REFERENCES `${table.prefix}users`(id)
   -- [jooq ignore stop]
);

CREATE TABLE IF NOT EXISTS `${table.prefix}users` (
   id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, -- user_id
   uuid TEXT NOT NULL,
   first_fish TEXT NOT NULL,
   last_fish TEXT NOT NULL,
   largest_fish TEXT NOT NULL,
   largest_length REAL NOT NULL,
   num_fish_caught INTEGER NOT NULL,
   total_fish_length REAL NOT NULL,
   competitions_won INTEGER NOT NULL,
   competitions_joined INTEGER NOT NULL,
   fish_sold INTEGER DEFAULT 0,
   money_earned REAL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `${table.prefix}transactions` (
   id TEXT NOT NULL PRIMARY KEY,
   user_id INTEGER NOT NULL,
   timestamp TEXT NOT NULL,
   -- [jooq ignore start]
   FOREIGN KEY (user_id) REFERENCES `${table.prefix}users`(id)
   -- [jooq ignore stop]
);

CREATE TABLE IF NOT EXISTS `${table.prefix}users_sales` (
   id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
   transaction_id TEXT NOT NULL,
   fish_name TEXT NOT NULL,
   fish_rarity TEXT NOT NULL,
   fish_amount INTEGER NOT NULL,
   fish_length REAL NOT NULL,
   price_sold REAL NOT NULL,
   -- [jooq ignore start]
   FOREIGN KEY (transaction_id) REFERENCES `${table.prefix}transactions`(id)
   -- [jooq ignore stop]
);
