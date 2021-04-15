package com.oheers.fish.fishing;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.database.Database;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class FishEvent implements Listener, Runnable {

    Player player;
    String name;
    Float length;

    private final List<String> breakabletools = Arrays.asList(
            "FISHING_ROD",
            "SHOVEL",
            "TRIDENT",
            "AXE",
            "BOW",
            "HOE",
            "SHEARS",
            "HELMET",
            "CHESTPLATE",
            "LEGGINGS",
            "BOOTS",
            "SHIELD",
            "CROSSBOW",
            "ELYTRA",
            "FLINT_AND_STEEL"
    );

    @EventHandler
    public void onFish(PlayerFishEvent event) {

        if (EvenMoreFish.mainConfig.getEnabled()) {

            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {

                if (competitionOnlyCheck()) {

                    Player player = event.getPlayer();

                    Fish fish = getFish(random(), event.getHook().getLocation().getBlock().getBiome());
                    fish.setFisherman(player);
                    fish.init();
                    // puts all the fish information into a format that Messages.renderMessage() can print out nicely

                    String length = Float.toString(fish.getLength());
                    String name = ChatColor.translateAlternateColorCodes('&', fish.getRarity().getColour() + "&l" + fish.getName());
                    String rarity = ChatColor.translateAlternateColorCodes('&', fish.getRarity().getColour() + "&l" + fish.getRarity().getValue());

                    // checks if the fish can have durability, and if it's set in the config it receives random durability
                    if (checkBreakable(fish.getType().getType())) fish.randomBreak();

                    Message msg = new Message()
                            .setMSG(EvenMoreFish.msgs.getFishCaught())
                            .setPlayer(player.getName())
                            .setColour(fish.getRarity().getColour())
                            .setLength(length)
                            .setFishCaught(name)
                            .setRarity(rarity);

                    // Gets whether it's a serverwide announce or not
                    if (fish.getRarity().getAnnounce()) {
                        // sends it to all online players
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage(msg.toString());
                        }
                    } else {
                        // sends it to just the fisher
                        player.sendMessage(msg.toString());
                    }

                    competitionCheck(fish, event.getPlayer());

                    // a much less smoothbrain way of dropping the fish
                    Item nonCustom = (Item) event.getCaught();
                    nonCustom.setItemStack(fish.give(event.getPlayer()));

                    if (EvenMoreFish.mainConfig.isDatabaseOnline()) {
                        this.player = player;
                        this.name = fish.getName();
                        this.length = fish.getLength();
                        Thread t1 = new Thread(this);
                        t1.start();
                    }
                }
            }
        }
    }

    private Rarity random() {
        // Loads all the rarities
        List<Rarity> rarities = new ArrayList<>(EvenMoreFish.fishCollection.keySet());

        double totalWeight = 0;

        // Weighted random logic (nabbed from stackoverflow)
        for (Rarity r : rarities) {
            totalWeight += r.getWeight();
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < rarities.size() - 1; ++idx) {
            r -= rarities.get(idx).getWeight();
            if (r <= 0.0) break;
        }

        return rarities.get(idx);
    }

    private Fish getFish(Rarity r, Biome b) {
        // the fish that are of (Rarity r)
        List<Fish> rarityFish = EvenMoreFish.fishCollection.get(r);
        // will store all the fish that match the player's biome or don't discriminate biomes
        List<Fish> available = new ArrayList<>();

        for (Fish f : rarityFish) {

            if (f.getBiomes().contains(b) || f.getBiomes().size()==0) {
                available.add(f);
            }
        }

        // if the config doesn't define any fish that can be fished in this biome.
        if (available.size() == 0) {
            Bukkit.getLogger().log(Level.WARNING, "There are no fish of the rarity " + r.getValue() + " that can be fished in the " + b.name() + " biome.");
            return defaultFish();
        }

        int ran = (int) (Math.random() * available.size());
        return available.get(ran);
    }

    // if there's no fish available in the current biome, this gets sent out
    private Fish defaultFish() {
        Rarity r = new Rarity("No biome found", "&4", 1.0d, false);
        return new Fish(r, "");
    }

    // Checks if it should be giving the player the fish considering the fish-only-in-competition option in config.yml
    private boolean competitionOnlyCheck() {
        if (EvenMoreFish.mainConfig.isCompetitionUnique()) {
            return EvenMoreFish.active != null;
        } else {
            return true;
        }
    }

    private void competitionCheck(Fish fish, Player fisherman) {
        if (EvenMoreFish.active != null) {
            EvenMoreFish.active.runLeaderboardScan(fisherman, fish);
        }
    }

    private boolean checkBreakable(Material material) {
        if (EvenMoreFish.mainConfig.doingRandomDurability()) {
            for (String s : breakabletools) {
                if (material.toString().contains(s)) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    @Override
    public void run() {
        try {

            // increases the fish fished count if the fish is already in the db
            if (Database.hasFish(name)) {
                Database.fishIncrease(name);

                // sets the new leader in top fish, if the player has fished a record fish
                if (Database.getTopLength(name) < length) {
                    Database.newTopSpot(player, name, length);
                }
            } else {
                // the database doesn't contain the fish yet
                Database.add(name, player, length);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
