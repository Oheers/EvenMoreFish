CREATE TABLE IF NOT EXISTS`${table.prefix}transactions` (
   id VARCHAR(22) NOT NULL,
   user_id INTEGER NOT NULL,
   timestamp TIMESTAMP NOT NULL,
   PRIMARY KEY(id)
);


CREATE TABLE IF NOT EXISTS `${table.prefix}users_sales` (
   id INTEGER NOT NULL ${auto.increment},
   transaction_id VARCHAR(22) NOT NULL,
   fish_name VARCHAR(256) NOT NULL,
   fish_rarity VARCHAR(256) NOT NULL,
   fish_amount INTEGER NOT NULL,
   fish_length DOUBLE NOT NULL,
   price_sold DOUBLE NOT NULL,
   ${primary.key}
);