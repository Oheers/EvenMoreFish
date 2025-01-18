package com.oheers.fish.database.migrate;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.PrefixType;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.connection.MigrationManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jooq.impl.DSL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class LegacyToV3DatabaseMigration {
    private final Database database;
    private final MigrationManager migrationManager;
    
    public LegacyToV3DatabaseMigration(final Database database, final MigrationManager migrationManager) {
        this.database = database;
        this.migrationManager = migrationManager;
    }
    
    /**
     * This causes a renaming of the table "Fish2" to "emf_fish", no data internally changes, but it's good to have a clean
     * format for all the tables and to have a more descriptive name for this stuff.
     */
    private void translateFishDataV2() {
        if (migrationManager.queryTableExistence("${table.prefix}fish")) {
            return;
        }
        
        if (migrationManager.queryTableExistence("Fish2")) {
            database.executeStatement(c -> c.alterTable("Fish2")
                    .renameTo("${table.prefix}fish")
                    .execute());
            return;
        }
        migrationManager.legacyInitVersion();
    }
    
    /**
     * Loops through each fish report passed through and sets all the default values for the user in the database. Note
     * that the user must already have a field within the database. All data regarding competitions and the total size does
     * not exist within the V2 and V1 recording system so are set to 0 by default, as this lost data cannot be recovered.
     * Similarly, the latest fish cannot be retrieved, so the "None" fish is left, to reference there is no value here yet.
     * <p>
     * This should only be used during the V1/2 -> V3 migration process.
     *
     * @param uuid    The user
     * @param reports The V2 fish reports associated with the user.
     */
    private void translateFishReportsV2(final UUID uuid, final @NotNull List<LegacyFishReport> reports) {
        String firstFishID = "";
        long epochFirst = Long.MAX_VALUE;
        String largestFishID = "";
        float largestSize = 0f;
        
        int totalFish = 0;
        
        for (LegacyFishReport report : reports) {
            if (report.getTimeEpoch() < epochFirst) {
                epochFirst = report.getTimeEpoch();
                firstFishID = report.getRarity() + ":" + report.getName();
            }
            if (report.getLargestLength() > largestSize) {
                largestSize = report.getLargestLength();
                largestFishID = report.getRarity() + ":" + report.getName();
            }
            
            totalFish += report.getNumCaught();
            var fishLog = DSL.table(MainConfig.getInstance().getPrefix() + "fish_log");
            var id = DSL.field("id", Integer.class);
            var rarity = DSL.field("rarity", String.class);
            var fish = DSL.field("fish", String.class);
            var quantity = DSL.field("quantity", Integer.class);
            var fishCatchTime = DSL.field("first_catch_time", Long.class);
            var largestLength = DSL.field("largest_length", Float.class);

            database.executeStatement(c -> c.insertInto(fishLog)
                    .set(id, database.getUserId(uuid))
                    .set(rarity, report.getRarity())
                    .set(fish, report.getName())
                    .set(quantity, report.getNumCaught())
                    .set(fishCatchTime, report.getTimeEpoch())
                    .set(largestLength, report.getLargestLength())
                    .execute());
            // starts a field for the new fish for the user that's been fished for the first time
            
        }
        
        createFieldForFishFirstTimeFished(uuid, firstFishID, largestFishID, totalFish, largestSize);
    }
    
    private void createFieldForFishFirstTimeFished(final UUID uuid, final String firstFishID, final String largestFishID, int totalFish, float largestSize) {
        var usersTable = DSL.table(MainConfig.getInstance().getPrefix() + "users");
        var firstFishField = DSL.field("first_fish", String.class);
        var largestFishField = DSL.field("largest_fish", String.class);
        var totalFishField = DSL.field("num_fish_caught", Integer.class);
        var largestLengthField = DSL.field("largest_length", Float.class);
        var uuidField = DSL.field("uuid", String.class);

        // starts a field for the new fish that's been fished for the first time
        database.executeStatement(c -> c.update(usersTable)
                .set(firstFishField,firstFishID)
                .set(largestFishField, largestFishID)
                .set(totalFishField, totalFish)
                .set(largestLengthField, largestSize)
                .where(uuidField.eq(uuid.toString()))
                .execute());
    }
    
    /**
     * Converts a V2 database system to a V3 database system. The server must not crash during this process as this may
     * lead to data loss, but honestly, I'm not 100% sure on that one. Data is read from the /data/ folder and is
     * inserted into the new database system then the /data/ folder is renamed to /data-old/.
     *
     * @param initiator The person who started the migration.
     *
     */
    public void migrate(CommandSender initiator) {
        if (!migrationManager.usingV2()) {
            Message msg = new Message("EvenMoreFish is already using the latest V3 database engine.");
            msg.usePrefix(PrefixType.ERROR);
            msg.broadcast(initiator);
            return;
        }
        
        EvenMoreFish.getInstance().getLogger().info(() -> initiator.getName() + " has begun the migration to EMF database V3 from V2.");
        Message msg = new Message("Beginning conversion to V3 database engine.");
        msg.usePrefix(PrefixType.ADMIN);
        msg.broadcast(initiator);
        
        File oldDataFolder = new File(EvenMoreFish.getInstance().getDataFolder(), "data");
        File dataFolder = new File(EvenMoreFish.getInstance().getDataFolder(), "data-archived");
        
        if (oldDataFolder.renameTo(dataFolder)) {
            Message message = new Message("Archived /data/ folder.");
            message.usePrefix(PrefixType.ADMIN);
            message.broadcast(initiator);
        } else {
            Message message = new Message("Failed to archive /data/ folder. Cancelling migration. [No further information]");
            message.usePrefix(PrefixType.ADMIN);
            message.broadcast(initiator);
            return;
        }
        
        Message fishReportMSG = new Message("Beginning FishReport migrations. This may take a while.");
        fishReportMSG.usePrefix(PrefixType.ADMIN);
        fishReportMSG.broadcast(initiator);
        
        try {
            translateFishDataV2();
            this.migrationManager.legacyFlywayBaseline();
            
            for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
                Type fishReportList = new TypeToken<List<LegacyFishReport>>() {
                }.getType();
                
                Gson gson = new Gson();
                List<LegacyFishReport> reports;
                try(FileReader reader = new FileReader(file)) {
                    reports = gson.fromJson(reader, fishReportList);
                }

                UUID playerUUID = UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf(".")));
                database.createUser(playerUUID);
                translateFishReportsV2(playerUUID, reports);
                
                Message migratedMSG = new Message("Migrated " + reports.size() + " fish for: " + playerUUID);
                migratedMSG.usePrefix(PrefixType.ADMIN);
                migratedMSG.broadcast(initiator);
            }
            
        } catch (NullPointerException | FileNotFoundException exception) {
            Message message = new Message("Fatal error whilst upgrading to V3 database engine.");
            message.usePrefix(PrefixType.ERROR);
            message.broadcast(initiator);
            
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Critical SQL/interruption error whilst upgrading to v3 engine.", exception);
        } catch (IOException e) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        
        Message migratedMSG = new Message("Migration completed. Your database is now using the V3 database engine: to complete the migration, it is recommended to restart your server.");
        migratedMSG.usePrefix(PrefixType.ERROR);
        migratedMSG.broadcast(initiator);
        
        Message thankyou = new Message("Now that migration is complete, you will be able to use functionality in upcoming" +
            " updates such as quests, deliveries and a fish log. - Oheers");
        thankyou.usePrefix(PrefixType.ERROR);
        thankyou.broadcast(initiator);

        //Run the rest of the migrations, and ensure it's properly setup.
        migrationManager.migrateFromV5ToLatest();
    }
}
