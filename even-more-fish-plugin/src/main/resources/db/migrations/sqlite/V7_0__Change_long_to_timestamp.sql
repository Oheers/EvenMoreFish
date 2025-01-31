-- Step 1: Create a new table with the desired schema
CREATE TABLE `${table.prefix}fish_log_new` (
   id INTEGER PRIMARY KEY,
   rarity VARCHAR(128) NOT NULL,
   fish VARCHAR(128) NOT NULL,
   quantity INT NOT NULL,
   first_catch_time TIMESTAMP NULL,
   largest_length REAL NOT NULL,
   -- [jooq ignore start]
   CONSTRAINT FK_FishLog_User
   FOREIGN KEY (id) REFERENCES `${table.prefix}users(id)`,
     -- [jooq ignore stop]
);

-- Step 2: Populate the new table
INSERT INTO `${table.prefix}fish_log_new` (id, rarity, fish, quantity, first_catch_time, largest_length)
SELECT id, rarity, fish, quantity, DATETIME(first_catch_time / 1000, 'unixepoch'), largest_length
FROM `${table.prefix}fish_log`;

-- Step 3: Drop the old table
DROP TABLE `${table.prefix}fish_log`;

-- Step 4: Rename the new table
ALTER TABLE `${table.prefix}fish_log_new` RENAME TO `${table.prefix}fish_log`;

-- Repeat the same steps for the fish table
CREATE TABLE `${table.prefix}fish_new` (
   fish_name VARCHAR(100) NOT NULL PRIMARY KEY,
   fish_rarity VARCHAR(100) NOT NULL,
   first_fisher VARCHAR(36) NOT NULL,
   total_caught INTEGER NOT NULL,
   largest_fish REAL NOT NULL,
   largest_fisher VARCHAR(36) NOT NULL,
   first_catch_time TIMESTAMP NOT NULL,
);

INSERT INTO `${table.prefix}fish_new` (fish_name, fish_rarity, first_fisher, total_caught, largest_fish, largest_fisher, first_catch_time)
SELECT fish_name, fish_rarity, first_fisher, total_caught, largest_fish, largest_fisher, DATETIME(first_catch_time / 1000, 'unixepoch')
FROM `${table.prefix}fish`;

DROP TABLE `${table.prefix}fish`;

ALTER TABLE `${table.prefix}fish_new` RENAME TO `${table.prefix}fish`;
