package com.oheers.fish;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FishUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");
    private static final char COLOR_CHAR = '\u00A7';

    /* checks for the "emf-fish-length" nbt tag, to determine if this itemstack is a fish or not.
     * we only need to check for the length since they're all added in a batch if it's an EMF fish */
    public static boolean isFish(ItemStack i) {
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");

        if (i != null) {
            if (i.hasItemMeta()) {
                return i.getItemMeta().getPersistentDataContainer().has(nbtlength, PersistentDataType.FLOAT);
            }
        }

        return false;
    }

    public static boolean isFish(Skull s) {
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");

        if (s != null) {
            return s.getPersistentDataContainer().has(nbtlength, PersistentDataType.FLOAT);
        }

        return false;
    }

    public static Fish getFish(ItemStack i) {
        NamespacedKey nbtrarity = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-name");
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");

        // all appropriate null checks can be safely assumed to have passed to get to a point where we're running this method.
        PersistentDataContainer container = i.getItemMeta().getPersistentDataContainer();
        String nameString = container.get(nbtname, PersistentDataType.STRING);
        String rarityString = container.get(nbtrarity, PersistentDataType.STRING);
        Float lengthFloat = container.get(nbtlength, PersistentDataType.FLOAT);

        // Generating an empty rarity
        Rarity rarity = null;
        // Hunting through the fish collection and creating a rarity that matches the fish's nbt
        for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
            if (r.getValue().equals(rarityString)) {
                rarity = new Rarity(r.getValue(), r.getColour(), r.getWeight(), r.getAnnounce(), r.overridenLore);
            }
        }

        // setting the correct length so it's an exact replica.
        Fish fish = new Fish(rarity, nameString);
        fish.setLength(lengthFloat);

        return fish;
    }

    public static Fish getFish(Skull s) {
        NamespacedKey nbtrarity = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-name");
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");

        // all appropriate null checks can be safely assumed to have passed to get to a point where we're running this method.
        PersistentDataContainer container = s.getPersistentDataContainer();
        String nameString = container.get(nbtname, PersistentDataType.STRING);
        String rarityString = container.get(nbtrarity, PersistentDataType.STRING);
        Float lengthFloat = container.get(nbtlength, PersistentDataType.FLOAT);

        // Generating an empty rarity
        Rarity rarity = null;
        // Hunting through the fish collection and creating a rarity that matches the fish's nbt
        for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
            if (r.getValue().equals(rarityString)) {
                rarity = new Rarity(r.getValue(), r.getColour(), r.getWeight(), r.getAnnounce(), r.overridenLore);
            }
        }

        // setting the correct length so it's an exact replica.
        Fish fish = new Fish(rarity, nameString);
        fish.setLength(lengthFloat);

        return fish;
    }

    public static void giveItems(List<ItemStack> items, Player player) {
        int slots = 0;

        for (ItemStack is : player.getInventory().getStorageContents()) {
            if (is == null) {
                slots++;
            }
        }

        for (ItemStack item : items) {
            if (slots > 0) {
                player.getInventory().addItem(item);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.5f);
            } else {
                new BukkitRunnable() {
                    public void run() {
                        player.getLocation().getWorld().dropItem(player.getLocation(), item);
                    }
                }.runTask(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("EvenMoreFish")));
            }
        }
    }

    public static boolean checkRegion(Location l) {
        // if there's any region plugin installed
        if (EvenMoreFish.guardPL != null) {
            // if the user has defined a region whitelist
            if (EvenMoreFish.mainConfig.regionWhitelist()) {

                // Gets a list of user defined regions
                List<String> whitelistedRegions = EvenMoreFish.mainConfig.getAllowedRegions();

                if (EvenMoreFish.guardPL.equals("worldguard")) {

                    // Creates a query for whether the player is stood in a protectedregion defined by the user
                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionQuery query = container.createQuery();
                    ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(l));

                    // runs the query
                    for (ProtectedRegion pr : set) {
                        if (whitelistedRegions.contains(pr.getId())) return true;
                    }
                    return false;
                } else if (EvenMoreFish.guardPL.equals("redprotect")) {
                    Region r = RedProtect.get().getAPI().getRegion(l);
                    // if the hook is in any redprotect region
                    if (r != null) {
                        // if the hook is in a whitelisted region
                        return whitelistedRegions.contains(r.getName());
                    }
                    return false;
                } else {
                    // the user has defined a region whitelist but doesn't have a region plugin.
                    Bukkit.getLogger().log(Level.WARNING, "Please install WorldGuard or RedProtect to enable region-specific fishing.");
                    return true;
                }
            }
            return true;
        }
        return true;
    }

    public static boolean checkWorld(Location l) {
        // if the user has defined a world whitelist
        if (EvenMoreFish.mainConfig.worldWhitelist()) {

            // Gets a list of user defined regions
            List<String> whitelistedWorlds = EvenMoreFish.mainConfig.getAllowedWorlds();

            if (whitelistedWorlds.contains(l.getWorld().getName())) return true;
            else return false;
        }

        return true;
    }

    // credit to https://www.spigotmc.org/members/elementeral.717560/
    public static String translateHexColorCodes(String message)
    {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static ItemStack get(String base64EncodedString) {
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        assert meta != null;
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64EncodedString));
        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        skull.setItemMeta(meta);
        return skull;
    }

    public static String timeFormat(int timeLeft) {
        String returning = "";
        int hours = timeLeft/3600;

        if (timeLeft >= 3600) {
            returning += hours + EvenMoreFish.msgs.getBarHour() + " ";
        }

        if (timeLeft >= 60) {
            returning += ((timeLeft%3600)/60) + EvenMoreFish.msgs.getBarMinute() + " ";
        }

        // Remaining seconds to always show, e.g. "1 minutes and 0 seconds left" and "5 seconds left"
        returning += (timeLeft%60) + EvenMoreFish.msgs.getBarSecond();
        return returning;
    }

    public static String timeRaw(int timeLeft) {
        String returning = "";
        int hours = timeLeft/3600;

        if (timeLeft >= 3600) {
            returning += hours + ":";
        }

        if (timeLeft >= 60) {
            returning += ((timeLeft%3600)/60) + ":";
        }

        // Remaining seconds to always show, e.g. "1 minutes and 0 seconds left" and "5 seconds left"
        returning += (timeLeft%60);
        return returning;
    }

    public static void broadcastFishMessage(Message msg, boolean actionBar) {
        if (EvenMoreFish.competitionConfig.broadcastOnlyRods()) {
            // sends it to all players holding ords
            if (actionBar) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getInventory().getItemInMainHand().getType().equals(Material.FISHING_ROD) || p.getInventory().getItemInOffHand().getType().equals(Material.FISHING_ROD)) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg.toString()));
                    }
                }
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getInventory().getItemInMainHand().getType().equals(Material.FISHING_ROD) || p.getInventory().getItemInOffHand().getType().equals(Material.FISHING_ROD)) {
                        p.sendMessage(msg.toString());
                    }
                }
            }
            // sends it to everyone
        } else {
            if (actionBar) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg.toString()));
                }
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) p.sendMessage(msg.toString());
            }

        }
    }
}
