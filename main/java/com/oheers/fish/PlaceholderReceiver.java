package com.oheers.fish;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class PlaceholderReceiver extends PlaceholderExpansion {

    private EvenMoreFish plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public PlaceholderReceiver(EvenMoreFish plugin){
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
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
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
    public String getIdentifier(){
        return "emf";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link org.bukkit.entity.Player Player}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        // %emf_competition_place_player_1% would return the player in first place of any possible competition.
        if (identifier.startsWith("competition_place_player_")) {
            if (Competition.isActive()) {
                // checking the leaderboard actually contains the value of place
                String[] brokendown = identifier.split("_");
                int place = Integer.parseInt(brokendown[brokendown.length - 1]);
                Competition competition = EvenMoreFish.active;
                if (competition.getLeaderboardSize() >= place) {
                    // getting "place" place in the competition
                    UUID uuid = EvenMoreFish.active.getLeaderboard().getPlayer(place);
                    if (uuid != null) {
                        // To be in the leaderboard the player must have joined
                        return Objects.requireNonNull(Bukkit.getOfflinePlayer(uuid)).getName();
                    }
                } else {
                    if (EvenMoreFish.msgs.shouldNullPlayerCompPlaceholder()) {
                        return "";
                    } else return FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getNoPlayerInposPlaceholder());


                }
            } else {
                if (EvenMoreFish.msgs.shouldNullPlayerCompPlaceholder()) {
                    return "";
                } else return FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getNoCompPlaceholder());
            }
        } else if (identifier.startsWith("competition_place_size_")) {
            if (Competition.isActive()) {
                // checking the leaderboard actually contains the value of place
                String[] brokendown = identifier.split("_");
                int place = Integer.parseInt(brokendown[brokendown.length - 1]);
                if (EvenMoreFish.active.getLeaderboardSize() >= place) {
                    // getting "place" place in the competition
                    float value = EvenMoreFish.active.getLeaderboard().getPlaceValue(place);
                    if (value != -1.0f) {
                        return Float.toString(value);
                    }
                } else {
                    if (EvenMoreFish.msgs.shouldNullSizeCompPlaceholder()) {
                        return "";
                    } else return FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getNoPlayerInposPlaceholder());
                }
            } else {
                if (EvenMoreFish.msgs.shouldNullSizeCompPlaceholder()) {
                    return "";
                } else return FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getNoCompPlaceholder());
            }
        } else if (identifier.startsWith("competition_place_fish_")) {
            if (Competition.isActive() && EvenMoreFish.active.getCompetitionType() == CompetitionType.LARGEST_FISH) {
                // checking the leaderboard actually contains the value of place
                String[] brokendown = identifier.split("_");
                int place = Integer.parseInt(brokendown[brokendown.length - 1]);
                if (EvenMoreFish.active.getLeaderboardSize() >= place) {
                    // getting "place" place in the competition
                    Fish fish = EvenMoreFish.active.getLeaderboard().getPlaceFish(place);
                    if (fish != null) {
                        return new Message()
                                .setMSG(EvenMoreFish.msgs.getFishFormat())
                                .setRarity(fish.getRarity().getValue())
                                .setFishCaught(fish.getName())
                                .setLength(Float.toString(fish.getLength()))
                                .setColour(fish.getRarity().getColour())
                                .toString();
                    }
                } else {
                    if (EvenMoreFish.msgs.shouldNullFishCompPlaceholder()) {
                        return "";
                    } else return FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getNoPlayerInposPlaceholder());
                }
            } else {
                if (EvenMoreFish.msgs.shouldNullFishCompPlaceholder()) {
                    return "";
                } else return FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getNoCompPlaceholder());
            }
        }

        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return null;
    }
}