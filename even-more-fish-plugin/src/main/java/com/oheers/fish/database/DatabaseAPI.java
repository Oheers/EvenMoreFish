package com.oheers.fish.database;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.database.model.FishReport;
import com.oheers.fish.database.model.UserReport;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Interface for database operations related to the fishing system.
 */
public interface DatabaseAPI {

    // User-related methods
    boolean hasUser(@NotNull UUID uuid);

    boolean hasUserLog(@NotNull UUID uuid);

    void createUser(@NotNull UUID uuid);

    int getUserId(@NotNull UUID uuid);

    // User report methods
    void writeUserReport(@NotNull UUID uuid, @NotNull UserReport report);

    UserReport readUserReport(@NotNull UUID uuid);

    // Fish-related methods
    boolean hasFishData(@NotNull Fish fish);

    void createFishData(@NotNull Fish fish, @NotNull UUID uuid);

    void incrementFish(@NotNull Fish fish);

    String getDiscoverer(@NotNull Fish fish);

    LocalDateTime getFirstCatchDateForPlayer(@NotNull Fish fish, @NotNull HumanEntity player);

    LocalDateTime getFirstCatchDate(@NotNull Fish fish);

    float getLargestFishSizeForPlayer(@NotNull Fish fish, @NotNull HumanEntity player);

    float getLargestFishSize(@NotNull Fish fish);

    int getAmountFishCaughtForPlayer(@NotNull Fish fish, @NotNull HumanEntity player);

    int getAmountFishCaught(@NotNull Fish fish);

    void updateLargestFish(@NotNull Fish fish, @NotNull UUID uuid);

    // Fish report methods
    List<FishReport> getFishReportsForPlayer(@NotNull UUID uuid);

    List<FishReport> getReportsForFish(@NotNull UUID uuid, @NotNull Fish fish);

    void addUserFish(@NotNull FishReport report, int userId);

    void updateUserFish(@NotNull FishReport report, int userId);

    void writeFishReports(@NotNull UUID uuid, @NotNull List<FishReport> reports);

    boolean userHasFish(@NotNull Fish fish, @NotNull HumanEntity user);

    boolean userHasFish(@NotNull String rarity, @NotNull String fish, int id);

    // Competition-related methods
    void createCompetitionReport(@NotNull Competition competition);

    // Transaction-related methods
    void createSale(
            @NotNull String transactionId,
            @NotNull String fishName,
            @NotNull String fishRarity,
            int fishAmount,
            double fishLength,
            double priceSold
    );

    void createTransaction(
            @NotNull String transactionId,
            int userId,
            @NotNull Timestamp timestamp
    );
}

