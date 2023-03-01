package com.oheers.fish.database.migrate;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.PrefixType;
import com.oheers.fish.database.DatabaseV3;
import com.oheers.fish.database.FishReport;
import com.oheers.fish.database.Table;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author sarhatabaot
 */
public class LegacyToV3DatabaseMigration {
    private static DatabaseV3 database;
    
    public static void init(final DatabaseV3 database) {
        if(database == null) {
            LegacyToV3DatabaseMigration.database = database;
        }
    }
    
    /**
     * This causes a renaming of the table "Fish2" to "emf_fish", no data internally changes, but it's good to have a clean
     * format for all the tables and to have a more descriptive name for this stuff.
     */
    private static void translateFishDataV2() {
        if (database.queryTableExistence(Table.EMF_FISH.getTableID())) {
            return;
        }
        
        if (database.queryTableExistence("Fish2")) {
            database.executeStatement(c -> {
                try (PreparedStatement preparedStatement = c.prepareStatement("ALTER TABLE Fish2 RENAME TO " + Table.EMF_FISH.getTableID() + ";")) {
                    preparedStatement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            return;
        }
        
        database.executeStatement(c -> {
            try (PreparedStatement preparedStatement = c.prepareStatement(Table.EMF_FISH.creationCode)) {
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        
    }
    
    /**
     * Loops through each fish report passed through and sets all the default values for the user in the database. Note
     * that the user must already have a field within the database. All data regarding competitions and the total size does
     * not exist within the V2 and V1 recording system so are set to 0 by default, as this lost data cannot be recovered.
     * Similarly, the latest fish cannot be retrieved so the "None" fish is left, to reference there is no value here yet.
     * <p>
     * This should only be used during the V1/2 -> V3 migration process.
     *
     * @param uuid    The user
     * @param reports The V2 fish reports associated with the user.
     */
    private static void translateFishReportsV2(final UUID uuid, final @NotNull List<FishReport> reports) {
        String firstFishID = "";
        long epochFirst = Long.MAX_VALUE;
        String largestFishID = "";
        float largestSize = 0f;
        
        int totalFish = 0;
        
        for (FishReport report : reports) {
            if (report.getTimeEpoch() < epochFirst) {
                epochFirst = report.getTimeEpoch();
                firstFishID = report.getRarity() + ":" + report.getName();
            }
            if (report.getLargestLength() > largestSize) {
                largestSize = report.getLargestLength();
                largestFishID = report.getRarity() + ":" + report.getName();
            }
            
            totalFish += report.getNumCaught();
            database.executeStatement(c -> {
                String emfFishLogSQL = "INSERT INTO emf_fish_log (id, rarity, fish, quantity, first_catch_time, largest_length) VALUES (?,?,?,?,?,?)";
                try (PreparedStatement prep = c.prepareStatement(emfFishLogSQL)) {
                    prep.setInt(1, database.getUserID(uuid));
                    prep.setString(2, report.getRarity());
                    prep.setString(3, report.getName());
                    prep.setInt(4, report.getNumCaught());
                    prep.setLong(5, report.getTimeEpoch());
                    prep.setFloat(6, report.getLargestLength());
                    prep.executeUpdate();
                } catch (SQLException exception) {
                    EvenMoreFish.logger.severe(() -> "Could not add " + uuid + " in the table: Users.");
                    exception.printStackTrace();
                }
            });
            // starts a field for the new fish for the user that's been fished for the first time
            
        }
        
        createFieldForFishFirstTimeFished(uuid, firstFishID, largestFishID, totalFish, largestSize);
    }
    
    private static void createFieldForFishFirstTimeFished(final UUID uuid, final String firstFishID, final String largestFishID, int totalFish, float largestSize) {
        String emfUsersSQL = "UPDATE emf_users SET first_fish = ?, largest_fish = ?, num_fish_caught = ?, largest_length = ? WHERE uuid = ?;";
        // starts a field for the new fish that's been fished for the first time
        database.executeStatement(c -> {
            try (PreparedStatement prep = c.prepareStatement(emfUsersSQL);) {
                prep.setString(1, firstFishID);
                prep.setString(2, largestFishID);
                prep.setInt(3, totalFish);
                prep.setFloat(4, largestSize);
                prep.setString(5, uuid.toString());
                
                prep.executeUpdate();
            } catch (SQLException exception) {
                EvenMoreFish.logger.severe(() -> "Could not add " + uuid + " in the table: emf_users.");
                exception.printStackTrace();
            }
        });
    }
    
    /**
     * Converts a V2 database system to a V3 database system. The server must not crash during this process as this may
     * lead to data loss, but honestly I'm not 100% sure on that one. Data is read from the /data/ folder and is
     * inserted into the new database system then the /data/ folder is renamed to /data-old/.
     *
     * @param initiator The person who started the migration.
     *
     */
    public static void migrate(CommandSender initiator) {
        if (!database.usingVersionV2()) {
            Message msg = new Message("EvenMoreFish is already using the latest V3 database engine.");
            msg.usePrefix(PrefixType.ERROR);
            msg.broadcast(initiator, true, false);
            return;
        }
        
        EvenMoreFish.logger.info(() -> initiator.getName() + " has begun the migration to EMF database V3 from V2.");
        Message msg = new Message("Beginning conversion to V3 database engine.");
        msg.usePrefix(PrefixType.ADMIN);
        msg.broadcast(initiator, true, false);
        
        File oldDataFolder = new File(JavaPlugin.getProvidingPlugin(DatabaseV3.class).getDataFolder() + "/data/");
        File dataFolder = new File(JavaPlugin.getProvidingPlugin(DatabaseV3.class).getDataFolder() + "/data-archived/");
        
        if (oldDataFolder.renameTo(dataFolder)) {
            Message message = new Message("Archived /data/ folder.");
            message.usePrefix(PrefixType.ADMIN);
            message.broadcast(initiator, true, false);
        } else {
            Message message = new Message("Failed to archive /data/ folder. Cancelling migration. [No further information]");
            message.usePrefix(PrefixType.ADMIN);
            message.broadcast(initiator, true, false);
            return;
        }
        
        Message fishReportMSG = new Message("Beginning FishReport migrations. This may take a while.");
        fishReportMSG.usePrefix(PrefixType.ADMIN);
        fishReportMSG.broadcast(initiator, true, false);
        
        try {
            translateFishDataV2();
            database.createTables(true);
            
            for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
                Type fishReportList = new TypeToken<List<FishReport>>() {
                }.getType();
                
                Gson gson = new Gson();
                List<FishReport> reports;
                try(FileReader reader = new FileReader(file)) {
                    reports = gson.fromJson(reader, fishReportList);
                }
                UUID playerUUID = UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf(".")));
                database.createUser(playerUUID);
                translateFishReportsV2(playerUUID, reports);
                
                Message migratedMSG = new Message("Migrated " + reports.size() + " fish for: " + playerUUID);
                migratedMSG.usePrefix(PrefixType.ADMIN);
                migratedMSG.broadcast(initiator, true, false);
            }
            
        } catch (NullPointerException | FileNotFoundException exception) {
            exception.printStackTrace();
            Message message = new Message("Fatal error whilst upgrading to V3 database engine.");
            message.usePrefix(PrefixType.ERROR);
            message.broadcast(initiator, true, false);
            
            EvenMoreFish.logger.log(Level.SEVERE, "Critical SQL/interruption error whilst upgrading to v3 engine.");
            exception.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        Message migratedMSG = new Message("Migration completed. Your database is now using the V3 database engine: you do" +
            " not need to restart or reload your server to complete the process.");
        migratedMSG.usePrefix(PrefixType.ERROR);
        migratedMSG.broadcast(initiator, true, false);
        
        Message thankyou = new Message("Now that migration is complete, you will be able to use functionality in upcoming" +
            " updates such as quests, deliveries and a fish log. - Oheers");
        thankyou.usePrefix(PrefixType.ERROR);
        thankyou.broadcast(initiator, true, false);
    
        database.setUsingV2(false);
    }
}
