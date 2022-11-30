package com.oheers.fish.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class DataManager {

    private static DataManager instance;

    private Cache<UUID, UserReport> userReportCache;
    private Cache<UUID, List<FishReport>> fishReportCache;

    private void setup() {
        Expiry<? super UUID, UserReport> userReportExpiry = new Expiry<UUID, UserReport>() {
            @Override
            public long expireAfterCreate(@NotNull UUID uuid, @NotNull UserReport userReport, long l) {
                return getCacheDuration(uuid);
            }

            @Override
            public long expireAfterUpdate(@NotNull UUID uuid, @NotNull UserReport userReport, long l, long l1) {
                return getCacheDuration(uuid);
            }

            @Override
            public long expireAfterRead(@NotNull UUID uuid, @NotNull UserReport userReport, long l, long l1) {
                return getCacheDuration(uuid);
            }
        };

        userReportCache = Caffeine.newBuilder().expireAfter(userReportExpiry).build();

        Expiry<? super UUID, List<FishReport>> fishReportExpiry = new Expiry<UUID, List<FishReport>>() {
            @Override
            public long expireAfterCreate(@NotNull UUID uuid, @NotNull List<FishReport> fishReports, long l) {
                return getCacheDuration(uuid);
            }

            @Override
            public long expireAfterUpdate(@NotNull UUID uuid, @NotNull List<FishReport> fishReports, long l, long l1) {
                return getCacheDuration(uuid);
            }

            @Override
            public long expireAfterRead(@NotNull UUID uuid, @NotNull List<FishReport> fishReports, long l, long l1) {
                return getCacheDuration(uuid);
            }
        };

        fishReportCache = Caffeine.newBuilder().expireAfter(fishReportExpiry).build();
    }

    /**
     * Checks whether the user is still online, if they are still online we want to keep the user report cached to prevent
     * uncaching and therefore having to fetch their data from the database, wasting resources. To achieve this, the value
     * of the maximum long is returned, this means the cache will keep the user for essentially an infinite amount of time
     * however if they're offline then 0 will be returned, causing them to be uncached immediately.
     *
     * @param uuid The UUID of the user who owns the user report.
     * @returns Long.MAX_VALUE if the user is online, 0 if not.
     */
    private long getCacheDuration(UUID uuid) {
        if (Bukkit.getPlayer(uuid) != null) {
            return Long.MAX_VALUE;
        } else return 0;
    }

    /**
     * Forces a user to be removed from internal cache, this is automatically done as a player leaves the server.
     *
     * @param uuid The UUID of the player.
     */
    public void uncacheUser(UUID uuid) {
        invalidateUser(uuid);
    }

    /**
     * Removes all cached user entries, only use this once data has been saved locally otherwise data loss will occur.
     */
    public void uncacheAll() {
        userReportCache.invalidateAll();
        fishReportCache.invalidateAll();
    }

    /**
     * Adds a user to the cache storage with the uuid of the user as they key. This will be saved until the user logs out
     * from the server.
     *
     * @param uuid The UUID of the user.
     * @param userReport The user's relevant user report.
     * @param fishReports A list of fish reports relevant to the user.
     */
    public void cacheUser(UUID uuid, UserReport userReport, List<FishReport> fishReports) {
        userReportCache.put(uuid, userReport);
        fishReportCache.put(uuid, fishReports);
    }

    /**
     * Gets a list of fish reports from the cache, if the returned value is null then a false is returned. If they are
     * present, true is returned.
     *
     * @param uuid The UUID of the user in question.
     * @return True if the user is present in cache, false if not.
     */
    public boolean containsUser(UUID uuid) {
        return fishReportCache.getIfPresent(uuid) != null;
    }

    /**
     * Sets the cached list of fish reports related to the user to be accessed going forward. This should be a modified
     * version of a previously fetched list of FishReports, otherwise data loss may occur.
     *
     * @param uuid The UUID of the user to have this list set to them in the cache.
     * @param reports An arraylist of fish reports to be set to the user.
     */
    public void putFishReportsCache(@NotNull final UUID uuid, @NotNull final List<FishReport> reports) {
        fishReportCache.put(uuid, reports);
    }

    /**
     * Setting a user report for the user. This will be used to save data to the database so make sure it is kept up
     * to date to prevent data loss.
     *
     * @param uuid The UUID of the user owning the UserReport.
     * @param report The report to be set to the user.
     */
    public void putUserReportCache(@NotNull final UUID uuid, @NotNull final UserReport report) {
        userReportCache.put(uuid, report);
    }

    /**
     * Returns the user report relevant to the user if currently stored in cache.
     *
     * @param uuid The UUId of the user to be queried.
     * @return A UserReport belonging to the user if they exist in cache, and null if not.
     */
    public UserReport getUserReportIfExists(UUID uuid) {
        return userReportCache.getIfPresent(uuid);
    }

    /**
     * Returns a list of fish reports relevant to the user if they are currently stored within cache.
     *
     * @param uuid The UUID of the user to be queried.
     * @return A list of fish reports belonging to the user if they exist in cache, and null if not.
     */
    public List<FishReport> getFishReportsIfExists(UUID uuid) {
        return fishReportCache.getIfPresent(uuid);
    }

    /**
     * @return All user reports present within the cache in no specific order.
     */
    public Collection<UserReport> getAllUserReports() {
        return userReportCache.asMap().values();
    }

    /**
     * @return A map of a list of fish reports against the relevant user.
     */
    public ConcurrentMap<UUID, List<FishReport>> getAllFishReports() {
        return fishReportCache.asMap();
    }

    private void invalidateUser(UUID uuid) {
        userReportCache.invalidate(uuid);
        fishReportCache.invalidate(uuid);
    }

    public static DataManager getInstance() {
        return instance;
    }

    public static void init() {
        if (instance == null) {
            instance = new DataManager();
            instance.setup();
        }
    }

}
