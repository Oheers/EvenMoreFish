package com.oheers.fish.competition.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;

public class Reward {

    RewardType type;
    String action;

    public Reward(String value) {
        String[] split = value.split(":");

        if (split.length < 2) {
            Bukkit.getLogger().log(Level.WARNING, value + " is not formatted correctly. It won't be given as a reward");
            this.type = RewardType.EMPTY;
        } else {
            StringBuilder action = new StringBuilder();
            for (int i=1; i<split.length; i++) action.append(split[i]);
            this.action = action.toString();

            switch (split[0].toUpperCase()) {
                case "COMMAND": this.type = RewardType.COMMAND;
                    break;
                case "EFFECT": this.type = RewardType.EFFECT;
                    break;
                case "ITEM": this.type = RewardType.ITEM;
                    break;
                case "MESSAGE": this.type = RewardType.MESSAGE;
                    break;
                case "MONEY": this.type = RewardType.MONEY;
                    break;
                default:
                    this.type = RewardType.EMPTY;
            }
        }
    }

    public void run(Player player) {
        switch (type) {
            case COMMAND:
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), action.replace("{player}", player.getName()));
                break;
            case EFFECT:
                String[] parsedEffect = action.split(",");
                // Adds a potion effect in accordance to the config.yml "EFFECT:" value
                player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(parsedEffect[0])), Integer.parseInt(parsedEffect[2])*20, Integer.parseInt(parsedEffect[1])));
                break;
            case ITEM:
                String[] parsedItem = action.split(",");
                FishUtils.giveItems(Collections.singletonList(new ItemStack(Material.getMaterial(parsedItem[0]), Integer.parseInt(parsedItem[1]))), player);
                break;
            case MESSAGE:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', action));
                break;
            case MONEY:
                EvenMoreFish.econ.depositPlayer(player, Integer.parseInt(action));
                break;
            default:
                Bukkit.getLogger().log(Level.SEVERE, "Error in loading a reward.");
        }
    }
}
