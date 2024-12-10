package com.oheers.fish;

import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.database.model.UserReport;
import com.oheers.fish.fishing.items.Fish;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class PlaceholderReceiver extends PlaceholderExpansion {
    
    private final EvenMoreFish plugin;
    
    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin The instance of our plugin.
     */
    public PlaceholderReceiver(EvenMoreFish plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist() {
        return true;
    }
    
    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister() {
        return true;
    }
    
    /**
     * The name of the person who created this expansion should go here.
     * <br>For convenience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public @NotNull String getIdentifier() {
        return "emf";
    }
    
    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     * <p>
     * For convenience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param player     A {@link org.bukkit.entity.Player Player}.
     * @param identifier A String containing the identifier/value.
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equalsIgnoreCase("competition_type")) {
            if (!Competition.isActive()) {
                return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING.getMessage().getLegacyMessage();
            }
            return EvenMoreFish.getInstance().getActiveCompetition().getCompetitionType().name();
        }

        if (identifier.equalsIgnoreCase("competition_type_format")) {
            if (!Competition.isActive()) {
                return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING.getMessage().getLegacyMessage();
            }

            CompetitionType competitionType = EvenMoreFish.getInstance().getActiveCompetition().getCompetitionType();
            return switch (competitionType) {
                case LARGEST_FISH -> ConfigMessage.COMPETITION_TYPE_LARGEST.getMessage().getLegacyMessage();
                case LARGEST_TOTAL -> ConfigMessage.COMPETITION_TYPE_LARGEST_TOTAL.getMessage().getLegacyMessage();
                case MOST_FISH -> ConfigMessage.COMPETITION_TYPE_MOST.getMessage().getLegacyMessage();
                case SPECIFIC_FISH -> ConfigMessage.COMPETITION_TYPE_SPECIFIC.getMessage().getLegacyMessage();
                case SPECIFIC_RARITY -> ConfigMessage.COMPETITION_TYPE_SPECIFIC_RARITY.getMessage().getLegacyMessage();
                case SHORTEST_FISH -> ConfigMessage.COMPETITION_TYPE_SHORTEST.getMessage().getLegacyMessage();
                case SHORTEST_TOTAL -> ConfigMessage.COMPETITION_TYPE_SHORTEST_TOTAL.getMessage().getLegacyMessage();
                default -> "";
            };
        }

        // %emf_competition_place_player_1% would return the player in first place of any possible competition.
        if (identifier.startsWith("competition_place_player_")) {
            if (!Competition.isActive()) {
                return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING.getMessage().getLegacyMessage();
            }
            
            // checking the leaderboard actually contains the value of place
            int place = Integer.parseInt(identifier.substring(25));
            if (!leaderboardContainsPlace(place)) {
                return ConfigMessage.PLACEHOLDER_NO_PLAYER_IN_PLACE.getMessage().getLegacyMessage();
            }
            
            // getting "place" place in the competition
            UUID uuid;
            try {
                uuid = EvenMoreFish.getInstance().getActiveCompetition().getLeaderboard().getEntry(place).getPlayer();
            } catch (NullPointerException exception) {
                uuid = null;
            }
            if (uuid != null) {
                // To be in the leaderboard the player must have joined
                return Objects.requireNonNull(Bukkit.getOfflinePlayer(uuid)).getName();
            }
        }

        if (identifier.startsWith("competition_place_size_")) {
            if (!Competition.isActive()) {
                return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING_SIZE.getMessage().getLegacyMessage();
            }
            if (!(EvenMoreFish.getInstance().getActiveCompetition().getCompetitionType() == CompetitionType.LARGEST_FISH ||
                EvenMoreFish.getInstance().getActiveCompetition().getCompetitionType() == CompetitionType.LARGEST_TOTAL)) {
                return ConfigMessage.PLACEHOLDER_SIZE_DURING_MOST_FISH.getMessage().getLegacyMessage();
            }
            
            // checking the leaderboard actually contains the value of place
            int place = Integer.parseInt(identifier.substring(23));
            if (!leaderboardContainsPlace(place)) {
                return ConfigMessage.PLACEHOLDER_NO_SIZE_IN_PLACE.getMessage().getLegacyMessage();
            }
            
            // getting "place" place in the competition
            float value;
            try {
                value = EvenMoreFish.getInstance().getActiveCompetition().getLeaderboard().getEntry(place).getValue();
            } catch (NullPointerException exception) {
                value = -1;
            }
            
            if (value != -1.0f) {
                return Float.toString(Math.round(value * 10f) / 10f);
            } else {
                return "";
            }
        }

        if (identifier.startsWith("competition_place_fish_")) {
            if (!Competition.isActive()) {
                return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING_FISH.getMessage().getLegacyMessage();
            }

            int place = Integer.parseInt(identifier.substring(23));
            if (EvenMoreFish.getInstance().getActiveCompetition().getCompetitionType() == CompetitionType.LARGEST_FISH) {
                // checking the leaderboard actually contains the value of place
                if (!leaderboardContainsPlace(place)) {
                    return ConfigMessage.PLACEHOLDER_NO_FISH_IN_PLACE.getMessage().getLegacyMessage();
                }
                
                // getting "place" place in the competition
                Fish fish;
                try {
                    fish = EvenMoreFish.getInstance().getActiveCompetition().getLeaderboard().getEntry(place).getFish();
                } catch (NullPointerException exception) {
                    fish = null;
                }
                if (fish != null) {
                    AbstractMessage message = ConfigMessage.PLACEHOLDER_FISH_FORMAT.getMessage();
                    if (fish.getLength() == -1) {
                        message.setMessage(ConfigMessage.PLACEHOLDER_FISH_LENGTHLESS_FORMAT.getMessage());
                    } else {
                        message.setLength(Float.toString(fish.getLength()));
                    }
                    
                    message.setRarityColour(fish.getRarity().getColour());
                    message.setFishCaught(fish.getDisplayName());
                    message.setRarity(fish.getRarity().getDisplayName());
                    return message.getLegacyMessage();
                }
                
            } else {
                // checking the leaderboard actually contains the value of place
                float value;
                try {
                    value = Competition.leaderboard.getEntry(place).getValue();
                } catch (NullPointerException exception) {
                    value = -1;
                }

                if (value == -1) {
                    return ConfigMessage.PLACEHOLDER_NO_FISH_IN_PLACE.getMessage().getLegacyMessage();
                }
                
                AbstractMessage message = ConfigMessage.PLACEHOLDER_FISH_MOST_FORMAT.getMessage();
                message.setAmount(Integer.toString((int) value));
                return message.getLegacyMessage();
            }
            
        }
        
        if (identifier.startsWith("total_money_earned_")) {
            try {
                final UUID uuid = UUID.fromString(identifier.split("total_money_earned_")[1]);
                final UserReport userReport = DataManager.getInstance().getUserReportIfExists(uuid);
                if (userReport == null) {
                    return null;
                }
                return String.format("%.2f",userReport.getMoneyEarned());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        if (identifier.startsWith("total_fish_sold_")) {
            try {
                final UUID uuid = UUID.fromString(identifier.split("total_fish_sold_")[1]);
                final UserReport userReport = DataManager.getInstance().getUserReportIfExists(uuid);
                if (userReport == null) {
                    return null;
                }
                return String.valueOf(userReport.getFishSold());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        switch (identifier) {
            case "competition_time_left" -> {
                return Competition.getNextCompetitionMessage().getLegacyMessage();
            }
            case "competition_active" -> {
                return Boolean.toString(Competition.isActive());
            }
            case "custom_fishing_boolean" -> {
                return Boolean.toString(plugin.isCustomFishing(player));
            }
            case "custom_fishing_status" -> {
                if (plugin.isCustomFishing(player)) {
                    return ConfigMessage.CUSTOM_FISHING_ENABLED.getMessage().getLegacyMessage();
                } else {
                    return ConfigMessage.CUSTOM_FISHING_DISABLED.getMessage().getLegacyMessage();
                }
            }
        }

        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return null;
    }
    
    private boolean leaderboardContainsPlace(int place) {
        return EvenMoreFish.getInstance().getActiveCompetition().getLeaderboardSize() >= place;
    }
}