package com.oheers.fish.commands;

import com.oheers.fish.EvenMoreFish;
import dev.jorel.commandapi.CommandAPICommand;

import java.util.List;

public class AdminDatabaseCommand extends CommandAPICommand {
    public AdminDatabaseCommand() {
        super("database");

        withPermission("emf.admin.debug.database");
        setSubcommands(List.of(
                dropFlywayCommand(),
                repairFlywayCommand(),
                migrateToLatest()
        ));
    }

    public CommandAPICommand dropFlywayCommand() {
        return new CommandAPICommand("drop-flyway")
                .withPermission("emf.admin.debug.database.flyway")
                .withShortDescription("Drops the flyway schema history, useful for when the database breaks")
                .executes((commandSender, commandArguments) -> {
                            EvenMoreFish.getInstance().getDatabase().getMigrationManager().dropFlywaySchemaHistory();
                            commandSender.sendMessage("Dropped flyway schema history.");
                        }
                );
    }

    public CommandAPICommand repairFlywayCommand() {
        return new CommandAPICommand("repair-flyway")
                .withPermission("emf.admin.debug.database.flyway")
                .withShortDescription("Attempt to repair the database")
                .executes((commandSender, commandArguments) -> {
                    commandSender.sendMessage("Attempting to repair the migrations, check the logs.");
                    EvenMoreFish.getInstance().getDatabase().getMigrationManager().repairFlyway();
                });
    }

    public CommandAPICommand cleanFlywayCommand() {
        return new CommandAPICommand("clean-flyway")
                .withShortDescription("Attempt to clean the database")
                .withPermission("emf.admin.debug.database.clean")
                .executes((commandSender, commandArguments) -> {
                    commandSender.sendMessage("Attempting to clean flyway, check the logs.");
                    EvenMoreFish.getInstance().getDatabase().getMigrationManager().cleanFlyway();
                });
    }

    public CommandAPICommand migrateToLatest() {
        return new CommandAPICommand("migrate-to-latest")
                .withShortDescription("Migrate to the latest DB version.")
                .executes((commandSender, commandArguments) -> {
                    EvenMoreFish.getInstance().getDatabase().migrateFromDatabaseVersionToLatest();
                });
    }

}
