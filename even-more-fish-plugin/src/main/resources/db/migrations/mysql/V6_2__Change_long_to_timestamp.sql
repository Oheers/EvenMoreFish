-- we need to add a new column, convert the values, then drop the old column
-- Step 1: Add a new TIMESTAMP column
ALTER TABLE `${table.prefix}fish_log` ADD COLUMN first_catch_time_new TIMESTAMP NULL;

-- Step 2: Populate the new column
-- Assuming LONGBLOB stores Unix epoch values as binary
UPDATE `${table.prefix}fish_log`
SET first_catch_time_new = FROM_UNIXTIME(CAST(CONV(HEX(first_catch_time), 16, 10) AS UNSIGNED));

-- Step 3: Verify Data (Optional, for debugging purposes)
-- SELECT longblob_column, new_timestamp_column FROM your_table LIMIT 10;

-- Step 4: Drop the old LONGBLOB column
ALTER TABLE `${table.prefix}fish_log` DROP COLUMN first_catch_time;

-- Step 5: Rename the new column to the original name
ALTER TABLE `${table.prefix}fish_log` CHANGE first_catch_time_new first_catch_time TIMESTAMP;

-- Step 1: Add a new TIMESTAMP column
ALTER TABLE `${table.prefix}fish` ADD COLUMN first_catch_time_new TIMESTAMP NULL;

-- Step 2: Populate the new column
-- Assuming LONGBLOB stores Unix epoch values as binary
UPDATE `${table.prefix}fish`
SET first_catch_time_new = FROM_UNIXTIME(CAST(CONV(HEX(first_catch_time), 16, 10) AS UNSIGNED));

-- Step 3: Verify Data (Optional, for debugging purposes)
-- SELECT longblob_column, new_timestamp_column FROM your_table LIMIT 10;

-- Step 4: Drop the old LONGBLOB column
ALTER TABLE `${table.prefix}fish` DROP COLUMN first_catch_time;

-- Step 5: Rename the new column to the original name
ALTER TABLE `${table.prefix}fish` CHANGE first_catch_time_new first_catch_time TIMESTAMP;
