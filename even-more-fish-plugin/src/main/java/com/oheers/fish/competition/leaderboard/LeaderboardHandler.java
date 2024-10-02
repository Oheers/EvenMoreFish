package com.oheers.fish.competition.leaderboard;

import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface LeaderboardHandler {

    /**
     * @return A sorted list of entries. This list cannot be modified.
     */
    List<CompetitionEntry> getEntries();

    /**
     * @return The raw list of entries. This list is not sorted, but it can be modified.
     */
    List<CompetitionEntry> getRawEntries();

    /**
     * @return A list of unsorted UUIDs connected to the entry list
     */
    default List<UUID> getEntryUUIDs() {
        return getRawEntries().stream().map(CompetitionEntry::getPlayer).toList();
    }

    CompetitionType getCompetitionType();

    /**
     * Creates a new CompetitionEntry if one does not already exist.
     * @param player
     * @param fish
     * @return
     */
    default @NotNull CompetitionEntry addEntry(UUID player, Fish fish) {
        CompetitionEntry existingEntry = getEntry(player);
        if (existingEntry != null) {
            return existingEntry;
        }
        CompetitionEntry entry = new CompetitionEntry(player, fish, getCompetitionType());
        getRawEntries().add(entry);
        return entry;
    }

    default void addEntry(CompetitionEntry entry) {
        if (!contains(entry)) {
            getRawEntries().add(entry);
        }
    }

    default void clear() {
        getRawEntries().clear();
    }

    default boolean contains(CompetitionEntry entry) {
        return getEntry(entry.getPlayer()) != null;
    }

    default @Nullable CompetitionEntry getEntry(UUID player) {
        for (CompetitionEntry entry : getRawEntries()) {
            if (entry.getPlayer().equals(player)) {
                return entry;
            }
        }
        return null;
    }

    default CompetitionEntry getEntry(int place) {
        try {
            // This needs to use the sorted list
            return getEntries().get(place - 1);
        } catch (IndexOutOfBoundsException exception) {
            return null;
        }
    }

    default int getSize() {
        return getRawEntries().size();
    }

    default boolean hasEntry(UUID player) {
        for (CompetitionEntry entry : getRawEntries()) {
            if (entry.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }

    default void removeEntry(UUID player) {
        CompetitionEntry entry = getEntry(player);
        getRawEntries().remove(entry);
    }

    default CompetitionEntry getTopEntry() {
        return getEntries().get(0);
    }

    default void send(@NotNull CommandSender sender) {
        List<CompetitionEntry> sortedList = getEntries();
        int leaderboardCount = Messages.getInstance().getConfig().getInt("leaderboard-count");
        StringBuilder builder = new StringBuilder();
        List<UUID> loggedPlayers = new ArrayList<>();

        boolean playerDisplayed = false; // Track if player's entry is displayed
        UUID senderUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;

        // First, display the top 'leaderboardCount' players
        for (int pos = 0; pos < sortedList.size() && pos < leaderboardCount; pos++) {
            CompetitionEntry entry = sortedList.get(pos);

            appendMessageForEntry(entry, pos + 1, builder);
            loggedPlayers.add(entry.getPlayer());

            // Check if the sender is a player and their entry is already shown in the top 'leaderboardCount'
            if (senderUUID != null && entry.getPlayer().equals(senderUUID)) {
                playerDisplayed = true; // Mark player's entry as displayed
            }
        }

        // If sender is a player and their entry was not shown in the top 'leaderboardCount', show it after
        if (senderUUID != null && !playerDisplayed) {
            CompetitionEntry playerEntry = getEntry(senderUUID);

            if (playerEntry != null) {
                // Find the player's actual position in the sorted list
                int playerPosition = findPositionInList(sortedList, playerEntry);

                // Only append player's entry if they are not already within the leaderboard
                appendMessageForEntry(playerEntry, playerPosition, builder);
            }
        }

        // Send the final leaderboard message
        sender.sendMessage(builder.toString());
    }

    // Helper method to append message for an entry
    private void appendMessageForEntry(CompetitionEntry entry, int position, StringBuilder builder) {
        Message message = new Message(ConfigMessage.LEADERBOARD_LARGEST_FISH);
        message.setPlayer(Bukkit.getOfflinePlayer(entry.getPlayer()));
        message.setPosition(Integer.toString(position));
        message.setPositionColour("&f");

        Fish fish = entry.getFish();
        message.setRarityColour(fish.getRarity().getColour());
        message.setLength(Float.toString(entry.getValue()));
        message.setRarity(fish.getRarity().getDisplayName() != null ? fish.getRarity().getDisplayName() : fish.getRarity().getValue());
        message.setFishCaught(fish.getDisplayName() != null ? fish.getDisplayName() : fish.getName());

        builder.append(message.getRawMessage(true)).append("\n");
    }

    // Helper method to find player's position in the sorted list
    private int findPositionInList(List<CompetitionEntry> sortedList, CompetitionEntry playerEntry) {
        int position = 1; // Start from position 1
        for (CompetitionEntry entry : sortedList) {
            if (entry.equals(playerEntry)) {
                return position;
            }
            position++;
        }
        return -1; // Fallback if not found, though this shouldn't happen
    }

    default void sendToConsole() {
        send(Bukkit.getConsoleSender());
    }

    default void sendToAll() {
        Bukkit.getOnlinePlayers().forEach(this::send);
        sendToConsole();
    }

    void applyFish(@NotNull Fish fish, @NotNull Player player);

}

