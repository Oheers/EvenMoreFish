package com.oheers.fish.fishing;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class FishEvent implements Listener {



    @EventHandler
    public void onFish(PlayerFishEvent event) {

        if (MainConfig.enabled) {

            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {

                // Cancels the event then creates a fake catching 'animation'
                event.setCancelled(true);
                event.getHook().remove();

                Player player = event.getPlayer();

                Fish fish = new Fish(random(), player);

                // puts all the fish information into a format that Messages.renderMessage() can print out nicely

                String length = Float.toString(fish.getLength());
                String name = ChatColor.translateAlternateColorCodes('&', fish.getRarity().getColour() + "&l" + fish.getName());
                String rarity = ChatColor.translateAlternateColorCodes('&', fish.getRarity().getColour() + "&l" + fish.getRarity().getValue());

                String rendered = Messages.renderMessage(Messages.fishCaught, player.getName(), fish.getRarity().getColour(), length, name, rarity);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(rendered);
                }

                /* Drops the item rather than giving it straight to the player as a slap-dash way of checking the inventory
                 isn't full */
                Location location = event.getHook().getLocation();
                Location playerLoc = player.getLocation();

                // Drops it at the location of the hook, then spins it to face the player (hopefully)
                World world = location.getWorld();

                Item fishItem = player.getWorld().dropItem(location, fish.getItem());

                // Calculates differences between the player and rod, then divides by 10 to get a slightly smoother throw
                double xDif = (playerLoc.getX()-location.getX())/15;
                double yDif = ((playerLoc.getY()+5.5)-(location.getY()))/15;
                double zDif = (playerLoc.getZ()-location.getZ())/15;

                fishItem.setVelocity(new Vector(xDif, yDif, zDif));
                fishItem.setPickupDelay(0);
            }

        }
    }

    private Rarity random() {
        // Loads all the rarities
        List<Rarity> rarities = new ArrayList<>(EvenMoreFish.fish.keySet());

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
}
