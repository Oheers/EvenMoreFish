ALTER TABLE `${table.prefix}competitions`
  ALTER COLUMN contestants text;

ALTER TABLE `${table.prefix}fish`
  ADD PRIMARY KEY (fish_name);

ALTER TABLE `${table.prefix}fish_log`
  ADD CONSTRAINT FK_FishLog_User
  FOREIGN KEY(id) REFERENCES `${table.prefix}users(id)`;

ALTER TABLE `${table.prefix}users_sales`
  ADD CONSTRAINT FK_UsersSales_Transaction
  FOREIGN KEY (transaction_id) REFERENCES `${table.prefix}transactions(id)`;
