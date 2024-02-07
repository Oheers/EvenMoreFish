package com.oheers.fish.competition.reward;

import com.oheers.fish.Economy;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFRewardEvent;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.OldMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;

public class Reward {

    RewardType type;
    String action;

    Vector fishVelocity;

    public Reward(String value) {
        String[] split = value.split(":");

        if (split.length < 2) {
            EvenMoreFish.logger.log(Level.WARNING, value + " is not formatted correctly. It won't be given as a reward");
            this.type = RewardType.BAD_FORMAT;
        } else {
            try {
                // getting the value, imagine split being "COMMAND", "evenmorefish", "emf shop"
                this.type = RewardType.valueOf(split[0].toUpperCase());
                // joins the action by the removed ":" joiner
                this.action = StringUtils.join(split, ":", 1, split.length);

            } catch (IllegalArgumentException e) {
                this.type = RewardType.OTHER;
                // Sends the full reward out to the api if it's a custom reward
                this.action = value;
            }

        }
    }

    public String getAction() {
        return action;
    }

    public void run(OfflinePlayer player, Location hookLocation) {
        Player p;

        // Done like this to make the runnables not complain
        if (player.isOnline()) {
            p = (Player) player;
        } else {
            p = null;
        }

        switch (type) {
            case COMMAND:
                String inputCommand = this.action
                        .replace("{player}", player.getName());
                if (EvenMoreFish.usingPAPI) inputCommand = PlaceholderAPI.setPlaceholders(p, inputCommand);
                if (hookLocation != null) {
                    inputCommand = inputCommand
                            .replace("{x}", Double.toString(hookLocation.getX()))
                            .replace("{y}", Double.toString(hookLocation.getY()))
                            .replace("{z}", Double.toString(hookLocation.getZ()))
                            .replace("{world}", hookLocation.getWorld().getName());
                }

                // running the command
                String finalCommand = inputCommand;
                EvenMoreFish.getScheduler().callSyncMethod(() ->
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand));
                break;
            case EFFECT:
                if (p != null) {
                    String[] parsedEffect = action.split(",");
                    // Adds a potion effect in accordance to the config.yml "EFFECT:" value
                    EvenMoreFish.getScheduler().runTask(p, () -> p.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(parsedEffect[0])), Integer.parseInt(parsedEffect[2]) * 20, Integer.parseInt(parsedEffect[1]))));
                }

                break;
            case HEALTH:
                // checking the player doesn't have a special effect thingy on

                if (p != null) {
                    EvenMoreFish.getScheduler().runTask(p, () -> {
                        double newhealth = p.getHealth() + Integer.parseInt(action);
                        // checking the new health won't go above 20
                        p.setHealth(newhealth > 20 ? 20 : newhealth < 0 ? 0 : newhealth);
                    });
                }

                break;
            case HUNGER:

                if (p != null) {
                    EvenMoreFish.getScheduler().runTask(p, () -> p.setFoodLevel(p.getFoodLevel() + Integer.parseInt(action)));
                }

                break;
            case ITEM:
                if (p != null) {
                    String[] parsedItem = action.split(",");
                    FishUtils.giveItems(Collections.singletonList(new ItemStack(Material.getMaterial(parsedItem[0]), Integer.parseInt(parsedItem[1]))), p);
                }

                break;
            case MESSAGE:
                if (p != null) {
                    p.sendMessage(new OldMessage().setMSG(action).setReceiver(p).toString());
                }

                break;
            case MONEY:
                Economy economy = EvenMoreFish.economy;
                if (economy.isEnabled()) { economy.deposit(player, Integer.parseInt(action)); }
                break;
            // Specifically for when using Vault but also wanting to give points
            case PLAYER_POINTS:
                if (EvenMoreFish.usingPlayerPoints) {
                    PlayerPoints.getInstance().getAPI().give(player.getUniqueId(), Integer.parseInt(action));
                }
            case OTHER:
                PluginManager pM = Bukkit.getPluginManager();
                EMFRewardEvent event = new EMFRewardEvent(this, p, fishVelocity, hookLocation);
                pM.callEvent(event);
                break;
            default:
                EvenMoreFish.logger.log(Level.SEVERE, "Error in loading a reward.");
        }
    }

    public String format() {
        switch (type) {
            case COMMAND:
                if (MainConfig.getInstance().rewardCommand(action) != null) {
                    return new OldMessage()
                            .setMSG(MainConfig.getInstance().rewardCommand(action))
                            .toString();
                } else {
                    return new OldMessage()
                            .setMSG(action)
                            .toString();
                }
            case EFFECT:
                String[] parsedEffect = action.split(",");
                // Adds a potion effect in accordance to the config.yml "EFFECT:" value
                return new OldMessage()
                        .setMSG(MainConfig.getInstance().rewardEffect())
                        .setEffect(parsedEffect[0])
                        .setAmplifier(parsedEffect[1])
                        .setTime(FishUtils.timeFormat(Integer.parseInt(parsedEffect[2])))
                        .toString();
            case ITEM:
                String[] parsedItem = action.split(",");
                return new OldMessage()
                        .setMSG(MainConfig.getInstance().rewardItem())
                        .setItem(parsedItem[0])
                        .setAmount(parsedItem[1])
                        .toString();
            case MONEY:
                return new OldMessage()
                        .setMSG(MainConfig.getInstance().rewardMoney())
                        .setAmount(action)
                        .toString();
            case HEALTH:
                return new OldMessage()
                        .setMSG(MainConfig.getInstance().rewardHealth())
                        .setAmount(action)
                        .toString();
            case HUNGER:
                return new OldMessage()
                        .setMSG(MainConfig.getInstance().rewardHunger())
                        .setAmount(action)
                        .toString();
            default:
                return null;

        }
    }

    public void setFishVelocity(Vector fishVelocity) {
        this.fishVelocity = fishVelocity;
    }
}
