> 📖 Permission: `emf.admin.debug.database`
# Database Migration
EMF uses flyway to automatically migrate the database. Normally you shouldn't need to manually migrate the database.
In case things breaks you can use some commands to try and fix the issues.

> 📖 Permission: `emf.admin.debug.database.flyway`
## /emf admin database drop-flyway

Drops the flyway schema history table.
## /emf admin database repair-flyway
Runs the flyway repair command.
## /emf admin database clean-flyway
Runs the flyway clean command.
## /emf admin database migrate-to-latest
> 📖 Permission: `emf.admin.debug.database.migrate`
> 
Attempts to migrate the database to the latest version.


## Migrating from V2
Running the `/emf admin migrate` will attempt to migrate from database version 2 to the latest version.