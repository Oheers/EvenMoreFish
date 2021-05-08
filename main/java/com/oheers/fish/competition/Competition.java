package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Competition {

    // fisher, fish length
    public static HashMap<OfflinePlayer, Fish> leaderboardRegister;
    public static SortedMap<Fish, OfflinePlayer> leaderboardContents;

    Bar bar;

    // (seconds) the length of the competition
    int duration;

    public Competition(int duration) {

        leaderboardRegister = new HashMap<>();
        leaderboardContents = new TreeMap<>((a, b) -> b.getLength().compareTo(a.getLength()));
        this.duration = duration;
    }

    public void start(boolean adminStart) {
        // checks skip if it's started by an admin
        if (!adminStart) {
            // if there isn't enough players on, the competition doesn't start
            if (Bukkit.getServer().getOnlinePlayers().size() < EvenMoreFish.mainConfig.getMinimumPlayers()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getNotEnoughPlayers()));
                }

                return;
            }
        }

        announce();
        bar = new Bar(this.duration);
        EvenMoreFish.active = this;

    }

    public void end() {
        bar.end();
        EvenMoreFish.active = null;
        announceWinners();
    }

    public Bar getBar() {
        return bar;
    }

    private void announce() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getCompetitionStart()));
        }
    }

    // Returns the raw leaderboard register (sorted)
    public Map<Fish, OfflinePlayer> getLeaderboard() {
        return leaderboardContents;
    }

    // Returns the raw leaderboard contents (unsorted)
    public Map<OfflinePlayer, Fish> getRawLeaderboardContents() {
        return leaderboardRegister;
    }

    public void runLeaderboardScan(Player fisher, Fish fish) {
        // stuff
        for (OfflinePlayer holder : leaderboardRegister.keySet()) {
            if (holder == fisher) {
                if (fish.getLength() > leaderboardRegister.get(holder).getLength()) {
                    leaderboardContents.remove(leaderboardRegister.get(holder));
                    leaderboardRegister.put(fisher, fish);
                    leaderboardContents.put(fish, fisher);

                    return;
                }
                return;
            }
        }
        leaderboardRegister.put(fisher, fish);
        leaderboardContents.put(fish, fisher);
    }

    private void announceWinners() {
        getLeaderboard(true);
    }

    public static String getLeaderboard(boolean endingCompetition) {
        StringBuilder message = new StringBuilder();
        int position = 1;

        for (Map.Entry<Fish, OfflinePlayer> entry : leaderboardContents.entrySet()) {
            Fish f = entry.getKey();
            OfflinePlayer p = entry.getValue();

            if (endingCompetition) giveRewards(position, p);

            // creates a limit for the number of players to be shown
            if (position > EvenMoreFish.msgs.getLeaderboardCount()) {
                break;
            }

            // It's unlikely there'll be need for placeholders in a leaderboard output. Nulled.
            message.append(new Message(null)
                    .setMSG(EvenMoreFish.msgs.getLeaderboard())
                    .setPlayer(p.getName())
                    .setColour(f.getRarity().getColour())
                    .setLength(f.getLength().toString())
                    .setFishCaught(f.getName())
                    .setRarity(f.getRarity().getValue())
                    .setPosition(Integer.toString(position))
                    .toString());
            message.append("\n");
            position++;
        }

        if (message.toString().equals("")) {
            // returns that there's no scores yet, if the leaderboard is empty and it's not the end of the competition
            if (!endingCompetition) return FishUtils.translateHexColorCodes(EvenMoreFish.msgs.noWinners());
            // nobody fished a fish
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getCompetitionEnd()));
                player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.noWinners()));
                player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.noFish()));
            }

            return null;
        } else {
            if (!endingCompetition) return message.toString();

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getCompetitionEnd()));
                player.sendMessage(message.toString());
                // checks if the specific player didn't fish a fish
                if (!leaderboardRegister.containsKey(player)) player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.noFish()));
            }

            return null;
        }
    }

    private static void giveRewards(Integer position, OfflinePlayer player) {
        // checks if it's possible to actually give the rewards
        if (player.isOnline()) {
            // is there a reward set for the position player came?#
            if (EvenMoreFish.rewards.containsKey(position)) {
                // acts on each reward set for the player's position
                for (Reward r : EvenMoreFish.rewards.get(position)) {
                    r.run((Player) player);
                }
            }
        }

    }
}
