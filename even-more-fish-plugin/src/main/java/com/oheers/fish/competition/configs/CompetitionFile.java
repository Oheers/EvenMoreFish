package com.oheers.fish.competition.configs;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.competition.Bar;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.DayOfWeek;
import java.util.*;
import java.util.logging.Logger;

public class CompetitionFile extends ConfigBase {

    private static final Logger logger = EvenMoreFish.getInstance().getLogger();

    // We should never use the configUpdater for this.
    public CompetitionFile(@NotNull File file) throws InvalidConfigurationException {
        super(file, EvenMoreFish.getInstance(), false);
        performRequiredConfigChecks();
    }

    /**
     * Creates a new CompetitionFile with the provided values.
     * Used for the '/emf admin competition test' command.
     */
    public CompetitionFile(@NotNull String id, @NotNull CompetitionType type, int duration) {
        super();
        getConfig().set("id", id);
        getConfig().set("type", type.toString());
        getConfig().set("duration", duration);
    }

    // Current required config: id, type, times
    private void performRequiredConfigChecks() throws InvalidConfigurationException {
        if (getConfig().getString("id") == null) {
            logger.warning("Competition invalid: 'id' missing in " + getFileName());
            throw new InvalidConfigurationException("An ID has not been found in " + getFileName() + ". Please correct this.");
        }
        String type = getConfig().getString("type");
        if (type == null || CompetitionType.getType(type) == null) {
            logger.warning("Competition invalid: 'type' missing in " + getFileName());
            throw new InvalidConfigurationException("A type has not been found in " + getFileName() + ". Please correct this.");
        }
        if (getConfig().getInt("duration", -1) == -1) {
            logger.warning("Competition invalid: 'duration' missing in " + getFileName());
            throw new InvalidConfigurationException("A duration has not been found in " + getFileName() + ". Please correct this.");
        }
    }

    /**
     * @return The ID for this competition.
     */
    public @NotNull String getId() {
        return Objects.requireNonNull(getConfig().getString("id"));
    }

    /**
     * @return Should this competition be disabled?
     */
    public boolean isDisabled() {
        return getConfig().getBoolean("disabled");
    }

    /**
     * @return This competition's type.
     */
    public @NotNull CompetitionType getType() {
        return Objects.requireNonNull(CompetitionType.getType(getConfig().getString("type")));
    }

    /**
     * @return A list of times this competition should run at.
     */
    public @NotNull List<String> getTimes() {
        return getConfig().getStringList("times");
    }

    /**
     * @return A map of days and times to run this competition on.
     */
    public @NotNull Map<DayOfWeek, List<String>> getScheduledDays() {
        Section section = getConfig().getSection("days");
        Map<DayOfWeek, List<String>> dayMap = new HashMap<>();
        if (section == null) {
            return dayMap;
        }
        for (String dayStr : section.getRoutesAsStrings(false)) {
            DayOfWeek day = FishUtils.getDay(dayStr);
            if (day == null) {
                continue;
            }
            dayMap.put(day, section.getStringList(dayStr));
        }
        return dayMap;
    }

    /**
     * @return The duration of this competition (in minutes).
     */
    public int getDuration() {
        return Math.max(1, getConfig().getInt("duration"));
    }

    /**
     * @return The commands to execute when this competition starts.
     */
    public @NotNull List<String> getStartCommands() {
        String route = "start-commands";
        if (!getConfig().contains(route)) {
            return List.of();
        }
        if (getConfig().isList(route)) {
            return getConfig().getStringList(route);
        }
        return List.of(getConfig().getString(route));
    }

    /**
     * @return A list of days this competition should not run on.
     */
    public @NotNull List<DayOfWeek> getBlacklistedDays() {
        return getConfig().getStringList("blacklisted-days").stream()
                .map(FishUtils::getDay)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * @return A list of rarities that can be caught in this competition.
     */
    public @NotNull List<Rarity> getAllowedRarities() {
        return getConfig().getStringList("allowed-rarities").stream()
                .map(FishManager.getInstance()::getRarity)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * @return The number of fish needed for SPECIFIC_* competition types.
     */
    public int getNumberNeeded() {
        return Math.max(1, getConfig().getInt("number-needed"));
    }

    /**
     * @return Whether "fish caught" notifications only show for players holding fishing rods
     */
    public boolean shouldBroadcastOnlyRods() {
        return getConfig().getBoolean("broadcast-only-rods", true);
    }

    /**
     * @return The range of the "fish caught" notification, in blocks squared.
     */
    public int getBroadcastRange() {
        return getConfig().getInt("broadcast-range", -1);
    }

    /**
     * @return The colours to show for each winning position, if the {pos_colour} variable is used.
     */
    public @NotNull List<String> getPositionColours() {
        return getConfig().getStringList("leaderboard.position-colours", List.of("&6", "&e", "&7", "&7", "&8"));
    }

    public @NotNull List<Long> getAlertTimes() {
        List<String> times = getAlertTimesAsStrings();
        List<Long> finalTimes = new ArrayList<>();
        for (String time : times) {
            String[] split = time.split(":");
            if (split.length != 2) {
                logger.severe(time + " is not formatted correctly. Use MM:SS.");
                continue;
            }
            try {
                long seconds = Long.parseLong(split[1]);
                seconds += (Long.parseLong(split[0]) * 60);
                finalTimes.add(seconds);
            } catch (NumberFormatException exception) {
                logger.severe("Could not turn " + time + " into an alert time. If you need support, feel free to join the discord server: https://discord.gg/Hb9cj3tNbb");
            }
        }
        return finalTimes;
    }

    /**
     * @return The times to broadcast the "time remaining" message, represented as Strings.
     */
    public @NotNull List<String> getAlertTimesAsStrings() {
        return getConfig().getStringList("alerts");
    }

    /**
     * @return The rewards to be given for winners of this competition.
     */
    public @NotNull Map<Integer, List<Reward>> getRewards() {
        Section section = getConfig().getSection("rewards");
        if (section == null) {
            return Map.of();
        }
        Map<Integer, List<Reward>> rewardMap = new HashMap<>();
        for (String positionStr : section.getRoutesAsStrings(false)) {
            Integer position;
            if (positionStr.equalsIgnoreCase("participation")) {
                position = -1;
            } else {
                position = FishUtils.getInteger(positionStr);
            }
            if (position == null) {
                continue;
            }
            List<Reward> rewards = section.getStringList(positionStr).stream()
                    .map(Reward::new)
                    .toList();
            rewardMap.put(position, rewards);
        }
        return rewardMap;
    }

    /**
     * @return The colour of this competition's bossbar.
     */
    public @NotNull BarColor getBossbarColour() {
        String colour = getConfig().getString("bossbar-colour", "GREEN");
        try {
            return BarColor.valueOf(colour.toUpperCase());
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance().getLogger().warning(colour + " is not a valid bossbar colour. Defaulting to GREEN.");
            return BarColor.GREEN;
        }
    }

    /**
     * @return Whether this competition should show its bossbar.
     */
    public boolean shouldShowBossbar() {
        return getConfig().getBoolean("show-bossbar", true);
    }

    /**
     * @return The prefix for this competition's bossbar.
     */
    public Message getBossbarPrefix() {
        String prefix = getConfig().getString("bossbar-prefix", "&a&lFishing Contest: ");
        return new Message(prefix);
    }

    /**
     * @return A valid bossbar for this competition. Null if it should not be shown.
     */
    public @NotNull Bar createBossbar() {
        Bar bar = new Bar();
        bar.setShouldShow(shouldShowBossbar());
        bar.setColour(getBossbarColour());
        bar.setPrefix(getBossbarPrefix().getRawMessage());
        return bar;
    }

    /**
     * @return The amount of players required for this competition to start.
     */
    public int getPlayersNeeded() {
        return Math.max(1, getConfig().getInt("minimum-players"));
    }

    /**
     * @return The sound to play when this competition starts. Returns null if no sound should be played.
     */
    public @Nullable Sound getStartSound() {
        String soundString = getConfig().getString("start-sound", "NONE");
        if (soundString.equalsIgnoreCase("none")) {
            return null;
        }
        try {
            return Sound.valueOf(soundString.toUpperCase());
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance().getLogger().warning(soundString + " is not a valid sound. Defaulting to NONE.");
            return null;
        }
    }

    /**
     * @return The worlds this competition is valid in.
     */
    public List<World> getRequiredWorlds() {
        return getConfig().getStringList("required-worlds").stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .toList();
    }

}
