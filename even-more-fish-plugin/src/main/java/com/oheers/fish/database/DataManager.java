package com.oheers.fish.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.model.FishReport;
import com.oheers.fish.database.model.UserReport;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

//todo, this is caching manager, and really shouldn't be here in the database..
public class DataManager {

    private static DataManager instance;

    private Cache<UUID, UserReport> userReportCache;
    private Cache<UUID, List<FishReport>> fishReportCache;

    private void setup() {
        Expiry<? super UUID, UserReport> userReportExpiry = new Expiry<>() {
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

        Expiry<? super UUID, List<FishReport>> fishReportExpiry = new Expiry<>() {
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

    public void loadUserReportsIntoCache() {
        EvenMoreFish.getScheduler().runTaskAsynchronously(() -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                UserReport playerReport = EvenMoreFish.getInstance().getDatabase().readUserReport(player.getUniqueId());
                if (playerReport == null) {
                    EvenMoreFish.getInstance().getLogger().warning("Could not read report for player (" + player.getUniqueId() + ")");
                    continue;
                }
                DataManager.getInstance().putUserReportCache(player.getUniqueId(), playerReport);
            }
        });
    }

    /**
     * Checks whether the user is still online, if they are still online we want to keep the user report cached to prevent
     * uncaching and therefore having to fetch their data from the database, wasting resources. To achieve this, the value
     * of the maximum long is returned, this means the cache will keep the user for essentially an infinite amount of time
     * however if they're offline then 0 will be returned, causing them to be uncached immediately.
     *
     * @param uuid The UUID of the user who owns the user report.
     * @return Long.MAX_VALUE if the user is online, 0 if not.
     */
    private long getCacheDuration(UUID uuid) {
        if (Bukkit.getPlayer(uuid) != null) {
            return Long.MAX_VALUE;
        } else
            return 0;
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
     * @param uuid        The UUID of the user.
     * @param userReport  The user's relevant user report.
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
     * @param uuid    The UUID of the user to have this list set to them in the cache.
     * @param reports An arraylist of fish reports to be set to the user.
     */
    public void putFishReportsCache(@NotNull final UUID uuid, @NotNull final List<FishReport> reports) {
        fishReportCache.put(uuid, reports);
    }

    /**
     * Setting a user report for the user. This will be used to save data to the database so make sure it is kept up
     * to date to prevent data loss.
     *
     * @param uuid   The UUID of the user owning the UserReport.
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

    public void saveFishReports() {
        ConcurrentMap<UUID, List<FishReport>> allReports = DataManager.getInstance().getAllFishReports();
        EvenMoreFish.getInstance().getLogger().info("Saving " + allReports.size() + " fish reports.");
        for (Map.Entry<UUID, List<FishReport>> entry : allReports.entrySet()) {
            EvenMoreFish.getInstance().getDatabase().writeFishReports(entry.getKey(), entry.getValue());


            if (!EvenMoreFish.getInstance().getDatabase().hasUser(entry.getKey())) {
                EvenMoreFish.getInstance().getDatabase().createUser(entry.getKey());
            }

        }
    }

    public void saveUserReports() {
        EvenMoreFish.getInstance().getLogger().info("Saving " + DataManager.getInstance().getAllUserReports().size() + " user reports.");
        for (UserReport report : DataManager.getInstance().getAllUserReports()) {
            EvenMoreFish.getInstance().getDatabase().writeUserReport(report.getUUID(), report);
        }
    }


    /**
     * Adds the fish data to the live fish reports list, or changes the existing matching fish report. The plugin will
     * also account for a new top fish record and set any other data to a new fishing report for example the epoch
     * time.
     *
     * @param uuid The UUID of the user.
     * @param fish The fish object.
     */
    public void handleFishCatch(@NotNull final UUID uuid, @NotNull final Fish fish) {
        List<FishReport> cachedReports = getCachedReportsOrReports(uuid, fish);
        DataManager.getInstance().putFishReportsCache(uuid, cachedReports);

        UserReport report = DataManager.getInstance().getUserReportIfExists(uuid);

        if (report != null) {
            String fishID = fish.getRarity().getValue() + ":" + fish.getName();

            report.setRecentFish(fishID);
            report.incrementFishCaught(1);
            report.incrementTotalLength(fish.getLength());
            if (report.getFirstFish().equals("None")) {
                report.setFirstFish(fishID);
            }
            if (fish.getLength() > report.getLargestLength()) {
                report.setLargestFish(fishID);
                report.setLargestLength(fish.getLength());
            }

            DataManager.getInstance().putUserReportCache(uuid, report);
        }
    }


    private List<FishReport> getCachedReportsOrReports(final UUID uuid, final Fish fish) {
        List<FishReport> cachedReports = DataManager.getInstance().getFishReportsIfExists(uuid);
        if (cachedReports == null) {
            return List.of(
                    new FishReport(
                            fish.getRarity().getValue(),
                            fish.getName(),
                            fish.getLength(),
                            1,
                            -1
                    )
            );
        }

        for (FishReport report : cachedReports) {
            if (report.getRarity().equals(fish.getRarity().getValue()) && report.getName().equals(fish.getName())) {
                report.addFish(fish);
                return cachedReports;
            }
        }

        cachedReports.add(
                new FishReport(
                        fish.getRarity().getValue(),
                        fish.getName(),
                        fish.getLength(),
                        1,
                        -1
                )
        );
        return cachedReports;


    }
}
