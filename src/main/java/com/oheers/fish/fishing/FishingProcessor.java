package com.oheers.fish.fishing;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFFishEvent;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.FishReport;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class FishingProcessor implements Listener {

    @EventHandler
    public static void process(PlayerFishEvent event) {
        if (!isCustomFishAllowed()) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            ItemStack fish = getFish(event.getPlayer(), event.getHook().getLocation(), true, true);
            if (fish == null) {
                return;
            }

            // replaces the fishing item with a custom evenmorefish fish.
            Item nonCustom = (Item) event.getCaught();
            if (nonCustom != null) {
                if (fish.getType().isAir()) {
                    nonCustom.remove();
                } else {
                    nonCustom.setItemStack(fish);
                }
            }
        } else if (event.getState() == PlayerFishEvent.State.FISHING) {
            if (!EvenMoreFish.decidedRarities.containsKey(event.getPlayer().getUniqueId())) {
                EvenMoreFish.decidedRarities.put(event.getPlayer().getUniqueId(), randomWeightedRarity(event.getPlayer()));
            }

            /*
            if (EvenMoreFish.decidedRarities.get(event.getPlayer().getUniqueId()).isXmas2021()) {

                if (!Objects.equals(EvenMoreFish.xmas2021Config.getParticleMessage(), "none")) {
                    event.getPlayer().sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.xmas2021Config.getParticleMessage()));
                }

                if (EvenMoreFish.xmas2021Config.doXmas2021Particles()) {
                    ParticleEngine.renderParticles(event.getHook());
                }
            }
            - if the rarity is exposed by having particles showing
             */

        } // else if (event.getState() == PlayerFishEvent.State.REEL_IN) {
            /* For a failed attempt the player needs to have triggered a FISHING which generates a pre-decided rarity.
            if (EvenMoreFish.decidedRarities.get(event.getPlayer().getUniqueId()).isXmas2021()) {
                EvenMoreFish.decidedRarities.remove(event.getPlayer().getUniqueId());
            }

        } */
    }

    public static boolean isCustomFishAllowed() {
        return EvenMoreFish.mainConfig.getEnabled() && (competitionOnlyCheck() || EvenMoreFish.raritiesCompCheckExempt);
    }

    public static ItemStack getFish(Player player, Location location, boolean runRewards, boolean sendMessages) {
        if (!FishUtils.checkRegion(location, EvenMoreFish.mainConfig.getAllowedRegions())) {
            return null;
        }

        if (!FishUtils.checkWorld(location)) {
            return null;
        }

        Rarity fishRarity = randomWeightedRarity(player);
        if (fishRarity == null) return null;

        Fish fish = getFish(fishRarity, location, player);
        if (fish == null) return null;

        fish.setFisherman(player.getUniqueId());
        fish.init();

        if (runRewards && fish.hasFishRewards()) {
            for (Reward fishReward : fish.getFishRewards()) {
                fishReward.run(player);
            }
        }

        EMFFishEvent cEvent = new EMFFishEvent(fish, player);
        Bukkit.getPluginManager().callEvent(cEvent);

        if (sendMessages) {
            // puts all the fish information into a format that Messages.renderMessage() can print out nicely

            String length = Float.toString(fish.getLength());
            // Translating the colours because some servers store colour in their fish name
            String name = FishUtils.translateHexColorCodes(fish.getName());
            String rarity = FishUtils.translateHexColorCodes(fish.getRarity().getValue());

            Message msg = new Message()
                    .setMSG(EvenMoreFish.msgs.getFishCaught())
                    .setPlayer(player.getName())
                    .setRarityColour(fish.getRarity().getColour())
                    .setLength(length)
                    .setRarity(rarity)
                    .setReceiver(player);

            if (fish.getDisplayName() != null) msg.setFishCaught(fish.getDisplayName());
            else msg.setFishCaught(name);

            if (fish.getRarity().getDisplayName() != null) msg.setRarity(fish.getRarity().getDisplayName());
            else msg.setRarity(rarity);

            if (fish.getLength() != -1) {
                msg.setMSG(EvenMoreFish.msgs.getFishCaught());
            } else {
                msg.setMSG(EvenMoreFish.msgs.getLengthlessFishCaught());
            }

            // Gets whether it's a serverwide announce or not
            if (fish.getRarity().getAnnounce()) {
                // should we only broadcast this information to rod holders?
                FishUtils.broadcastFishMessage(msg, false);
            } else {
                // sends it to just the fisher
                player.sendMessage(msg.toString());
            }
        }

        try {
            competitionCheck(fish.clone(), player, location);
        } catch (CloneNotSupportedException e) {
            EvenMoreFish.logger.log(Level.SEVERE, "Failed to create a clone of: " + fish);
            e.printStackTrace();
        }

        if (EvenMoreFish.mainConfig.isDatabaseOnline()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {

                        // increases the fish fished count if the fish is already in the db
                        if (Database.hasFish(fish.getName())) {
                            Database.fishIncrease(fish.getName());

                            // sets the new leader in top fish, if the player has fished a record fish
                            if (Database.getTopLength(fish.getName()) < fish.getLength()) {
                                Database.newTopSpot(player, fish.getName(), fish.getLength());
                            }
                        } else {
                            // the database doesn't contain the fish yet
                            Database.add(fish, player);
                        }

                        boolean foundReport = false;

                        if (EvenMoreFish.fishReports.containsKey(player.getUniqueId())) {
                            for (FishReport report : EvenMoreFish.fishReports.get(player.getUniqueId())) {
                                if (report.getName().equals(fish.getName()) && report.getRarity().equals(fish.getRarity().getValue())) {
                                    report.addFish(fish);
                                    foundReport = true;
                                }
                            }
                        } else {
                            EvenMoreFish.fishReports.put(player.getUniqueId(), new ArrayList<>());
                        }

                        if (!foundReport) {
                            EvenMoreFish.fishReports.get(player.getUniqueId()).add(new FishReport(fish.getRarity().getValue(), fish.getName(), fish.getLength(), 1));
                        }

                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class));
        }

        return fish.give();
    }

    public static Rarity randomWeightedRarity(Player fisher) {

        if (EvenMoreFish.decidedRarities.containsKey(fisher.getUniqueId())) {
            Rarity chosenRarity = EvenMoreFish.decidedRarities.get(fisher.getUniqueId());
            EvenMoreFish.decidedRarities.remove(fisher.getUniqueId());
            return chosenRarity;
        }

        // Loads all the rarities
        List<Rarity> allowedRarities = new ArrayList<>();

        if (EvenMoreFish.permission != null) {
            for (Rarity rarity : EvenMoreFish.fishCollection.keySet()) {
                if (rarity.getPermission() != null) {
                    if (EvenMoreFish.permission.has(fisher, rarity.getPermission())) {
                        allowedRarities.add(rarity);
                    }
                } else {
                    allowedRarities.add(rarity);
                }
            }

        } else {
            allowedRarities.addAll(EvenMoreFish.fishCollection.keySet());
        }

        double totalWeight = 0;

        // Weighted random logic (nabbed from stackoverflow)
        for (Rarity r : allowedRarities) {
            totalWeight += r.getWeight();
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < allowedRarities.size() - 1; ++idx) {
            r -= allowedRarities.get(idx).getWeight();
            if (r <= 0.0) break;
        }

        if (allowedRarities.isEmpty()) {
            EvenMoreFish.logger.log(Level.SEVERE, "There are no rarities for the user " + fisher.getName() + " to fish. They have received no fish.");
            return null;
        }

        if (!Competition.isActive() && EvenMoreFish.raritiesCompCheckExempt) {
            if (allowedRarities.get(idx).hasCompExemptFish()) return allowedRarities.get(idx);
        } else if (Competition.isActive() || !EvenMoreFish.mainConfig.isCompetitionUnique()) {
            return allowedRarities.get(idx);
        }

        return null;
    }

    private static Fish randomWeightedFish(List<Fish> fishList) {
        double totalWeight = 0;

        // Weighted random logic (nabbed from stackoverflow)
        for (Fish fish : fishList) {
            totalWeight += fish.getWeight();
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < fishList.size() - 1; ++idx) {
            r -= fishList.get(idx).getWeight();
            if (r <= 0.0) break;
        }

        return fishList.get(idx);
    }

    public static Fish getFish(Rarity r, Location l, Player p) {
        if (r == null) return null;
        // will store all the fish that match the player's biome or don't discriminate biomes

        List<Fish> available = new ArrayList<>();

        // Protection against /emf admin reload causing the plugin to be unable to get the rarity
        if (EvenMoreFish.fishCollection.get(r) == null) r = randomWeightedRarity(p);


        for (Fish f : EvenMoreFish.fishCollection.get(r)) {

            if (EvenMoreFish.permission != null && f.getPermissionNode() != null) {
                if (!EvenMoreFish.permission.has(p, f.getPermissionNode())) {
                    continue;
                }
            }

            if (!FishUtils.checkRegion(l, f.getAllowedRegions())) {
                continue;
            }

            if (l.getWorld() != null) {
                if (f.getBiomes().contains(l.getBlock().getBiome()) || f.getBiomes().isEmpty()) {
                    available.add(f);
                }
            } else EvenMoreFish.logger.log(Level.SEVERE, "Could not get world for " + p.getUniqueId());
        }

        // if the config doesn't define any fish that can be fished in this biome.
        if (available.isEmpty()) {
            EvenMoreFish.logger.log(Level.WARNING, "There are no fish of the rarity " + r.getValue() + " that can be fished at (x=" + l.getX() + ", y=" + l.getY() + ", z=" + l.getZ() + ")");
            return null;
        }

        Fish returningFish;

        // checks whether weight calculations need doing for fish
        if (r.isFishWeighted()) {
            returningFish = randomWeightedFish(available);
        } else {
            int ran = ThreadLocalRandom.current().nextInt(available.size());
            returningFish = available.get(ran);
        }

        if (Competition.isActive() || !EvenMoreFish.mainConfig.isCompetitionUnique() || (EvenMoreFish.raritiesCompCheckExempt && returningFish.isCompExemptFish())) {
            return returningFish;
        } else {
            return null;
        }
    }

    // Checks if it should be giving the player the fish considering the fish-only-in-competition option in config.yml
    public static boolean competitionOnlyCheck() {
        if (EvenMoreFish.mainConfig.isCompetitionUnique()) {
            return Competition.isActive();
        } else {
            return true;
        }
    }

    public static void competitionCheck(Fish fish, Player fisherman, Location location) {
        if (Competition.isActive()) {
            if (!EvenMoreFish.competitionWorlds.isEmpty()) {
                if (location.getWorld() != null) {
                    if (!EvenMoreFish.competitionWorlds.contains(location.getWorld().getName())) {
                        return;
                    }
                } else {
                    EvenMoreFish.logger.log(Level.SEVERE, fisherman.getName() + " was unable to be checked for \"general.required-worlds\" in competitions.yml because their world is null.");
                }
            }

            EvenMoreFish.active.applyToLeaderboard(fish, fisherman);
        }
    }
}
