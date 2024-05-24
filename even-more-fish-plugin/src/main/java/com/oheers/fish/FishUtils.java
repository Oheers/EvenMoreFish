package com.oheers.fish;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.oheers.fish.config.CompetitionConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FishUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");
    private static final char COLOR_CHAR = '§';

    // checks for the "emf-fish-name" nbt tag, to determine if this ItemStack is a fish or not.
    public static boolean isFish(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return false;
        }

        return NbtUtils.hasKey(new NBTItem(item), NbtUtils.Keys.EMF_FISH_NAME);
    }

    public static boolean isFish(Skull skull) {
        if (skull == null) {
            return false;
        }

        return NbtUtils.hasKey(new NBTTileEntity(skull), NbtUtils.Keys.EMF_FISH_NAME);
    }

    public static Fish getFish(ItemStack item) {
        // all appropriate null checks can be safely assumed to have passed to get to a point where we're running this method.
        NBTItem nbtItem = new NBTItem(item);

        String nameString = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_NAME);
        String playerString = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_PLAYER);
        String rarityString = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_RARITY);
        Float lengthFloat = NbtUtils.getFloat(nbtItem, NbtUtils.Keys.EMF_FISH_LENGTH);
        Integer randomIndex = NbtUtils.getInteger(nbtItem, NbtUtils.Keys.EMF_FISH_RANDOM_INDEX);
        Integer xmasINT = NbtUtils.getInteger(nbtItem, NbtUtils.Keys.EMF_XMAS_FISH);
        boolean isXmasFish = false;

        if (xmasINT != null) {
            isXmasFish = xmasINT == 1;
        }

        if (nameString == null || rarityString == null) {
            return null; //throw new InvalidFishException("NBT Error");
        }


        // Generating an empty rarity
        Rarity rarity = null;
        // Hunting through the fish collection and creating a rarity that matches the fish's nbt
        for (Rarity r : EvenMoreFish.getInstance().getFishCollection().keySet()) {
            if (r.getValue().equals(rarityString)) {
                rarity = new Rarity(r.getValue(), r.getColour(), r.getWeight(), r.getAnnounce(), r.getUseConfigCasing(), r.overridenLore);
            }
        }

        // setting the correct length so it's an exact replica.
        try {
            Fish fish = new Fish(rarity, nameString, isXmasFish);
            if (randomIndex != null) {
                fish.getFactory().setType(randomIndex);
            }
            fish.setLength(lengthFloat);
            if (playerString != null) {
                fish.setFisherman(UUID.fromString(playerString));
            }

            return fish;
        } catch (InvalidFishException exception) {
            EvenMoreFish.getInstance().getLogger().severe("Could not create fish from an ItemStack with rarity " + rarityString + " and name " + nameString + ". You may have" +
                    "deleted the fish since this fish was caught.");
        }

        return null;
    }

    public static Fish getFish(Skull skull, Player fisher) throws InvalidFishException {
        // all appropriate null checks can be safely assumed to have passed to get to a point where we're running this method.
        final String nameString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtUtils.Keys.EMF_FISH_NAME).toString()));
        final String playerString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtUtils.Keys.EMF_FISH_PLAYER).toString()));
        final String rarityString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtUtils.Keys.EMF_FISH_RARITY).toString()));
        final Float lengthFloat = NBT.getPersistentData(skull, nbt -> nbt.getFloat(NbtUtils.getNamespacedKey(NbtUtils.Keys.EMF_FISH_LENGTH).toString()));
        final Integer randomIndex = NBT.getPersistentData(skull, nbt -> nbt.getInteger(NbtUtils.getNamespacedKey(NbtUtils.Keys.EMF_FISH_RANDOM_INDEX).toString()));
        final Integer xmasInteger = NBT.getPersistentData(skull, nbt -> nbt.getInteger(NbtUtils.getNamespacedKey(NbtUtils.Keys.EMF_XMAS_FISH).toString()));

        boolean isXmasFish = false;

        if (xmasInteger != null) {
            isXmasFish = xmasInteger == 1;
        }

        if (nameString == null || rarityString == null) {
            throw new InvalidFishException("NBT Error");
        }

        // Generating an empty rarity
        Rarity rarity = null;
        // Hunting through the fish collection and creating a rarity that matches the fish's nbt
        for (Rarity r : EvenMoreFish.getInstance().getFishCollection().keySet()) {
            if (r.getValue().equals(rarityString)) {
                rarity = new Rarity(r.getValue(), r.getColour(), r.getWeight(), r.getAnnounce(), r.getUseConfigCasing(), r.overridenLore);
            }
        }

        // setting the correct length and randomIndex, so it's an exact replica.
        Fish fish = new Fish(rarity, nameString, isXmasFish);
        fish.setLength(lengthFloat);
        if (randomIndex != null) {
            fish.getFactory().setType(randomIndex);
        }
        if (playerString != null) {
            try {
                fish.setFisherman(UUID.fromString(playerString));
            } catch (IllegalArgumentException ex) {
                fish.setFisherman(fisher.getUniqueId());
            }
        } else {
            fish.setFisherman(fisher.getUniqueId());
        }

        return fish;
    }

    public static void giveItems(List<ItemStack> items, Player player) {
        if (items.isEmpty()) {
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);
        player.getInventory().addItem(items.toArray(new ItemStack[0]))
                .values()
                .forEach(item -> EvenMoreFish.getScheduler().runTask(() -> player.getWorld().dropItem(player.getLocation(), item)));
    }

    public static void giveItems(ItemStack[] items, Player player) {
        if (items.length == 0) {
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);
        player.getInventory().addItem(items)
                .values()
                .forEach(item -> EvenMoreFish.getScheduler().runTask(() -> player.getWorld().dropItem(player.getLocation(), item)));
    }

    public static void giveItem(ItemStack item, Player player) {
        if (item == null) {
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);
        player.getInventory().addItem(item)
                .values()
                .forEach(loopItem -> EvenMoreFish.getScheduler().runTask(() -> player.getWorld().dropItem(player.getLocation(), loopItem)));
    }

    public static boolean checkRegion(Location l, List<String> whitelistedRegions) {
        // if there's any region plugin installed
        if (EvenMoreFish.getInstance().getGuardPL() == null) {
            return true;
        }
        // if the user has defined a region whitelist
        if (whitelistedRegions.isEmpty()) {
            return true;
        }

        if (EvenMoreFish.getInstance().getGuardPL().equals("worldguard")) {

            // Creates a query for whether the player is stood in a protectedregion defined by the user
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(l));

            // runs the query
            for (ProtectedRegion pr : set) {
                if (whitelistedRegions.contains(pr.getId())) {
                    return true;
                }
            }
            return false;
        } else if (EvenMoreFish.getInstance().getGuardPL().equals("redprotect")) {
            Region r = RedProtect.get().getAPI().getRegion(l);
            // if the hook is in any redprotect region
            if (r != null) {
                // if the hook is in a whitelisted region
                return whitelistedRegions.contains(r.getName());
            }
            return false;
        } else {
            // the user has defined a region whitelist but doesn't have a region plugin.
            EvenMoreFish.getInstance().getLogger().warning("Please install WorldGuard or RedProtect to enable region-specific fishing.");
            return true;
        }
    }

    public static boolean checkWorld(Location l) {
        // if the user has defined a world whitelist
        if (!MainConfig.getInstance().worldWhitelist()) {
            return true;
        }

        // Gets a list of user defined regions
        List<String> whitelistedWorlds = MainConfig.getInstance().getAllowedWorlds();
        if (l.getWorld() == null) {
            return false;
        } else {
            return whitelistedWorlds.contains(l.getWorld().getName());
        }
    }

    // credit to https://www.spigotmc.org/members/elementeral.717560/
    public static String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    //gets the item with a custom texture
    public static ItemStack get(String base64EncodedString) {
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            try {
                String decodedBase64 = new String(Base64.getDecoder().decode(base64EncodedString));
                URL url = new URL(decodedBase64.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decodedBase64.length() - "\"}}}".length()));
                PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "EMFFish");
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(url);
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            } catch (MalformedURLException ignored) {
                // If it's malformed, just don't set it.
            }
            skull.setItemMeta(meta);
        }
        return skull;
    }

    public static String timeFormat(long timeLeft) {
        String returning = "";
        long hours = timeLeft / 3600;
        long minutes = (timeLeft % 3600) / 60;
        long seconds = timeLeft % 60;

        if (hours > 0) {
            returning += new Message(ConfigMessage.BAR_HOUR_COLOR).getRawMessage(false) + hours + new Message(ConfigMessage.BAR_HOUR).getRawMessage(false) + " ";
        }

        if (minutes > 0) {
            returning += new Message(ConfigMessage.BAR_MINUTE_COLOR).getRawMessage(false) + minutes + new Message(ConfigMessage.BAR_MINUTE).getRawMessage(false) + " ";
        }

        // Shows remaining seconds if seconds > 0 or hours and minutes are 0, e.g. "1 minutes and 0 seconds left" and "5 seconds left"
        if (seconds > 0 || (minutes == 0 && hours == 0)) {
            returning += new Message(ConfigMessage.BAR_SECOND_COLOR).getRawMessage(false) + seconds + new Message(ConfigMessage.BAR_SECOND).getRawMessage(false) + " ";
        }

        return returning;
    }

    public static String timeRaw(long timeLeft) {
        String returning = "";
        long hours = timeLeft / 3600;

        if (timeLeft >= 3600) {
            returning += hours + ":";
        }

        if (timeLeft >= 60) {
            returning += ((timeLeft % 3600) / 60) + ":";
        }

        // Remaining seconds to always show, e.g. "1 minutes and 0 seconds left" and "5 seconds left"
        returning += (timeLeft % 60);
        return returning;
    }

    public static void broadcastFishMessage(Message message, Player referencePlayer, boolean actionBar) {

        String formatted = message.getRawMessage(true);

        if (formatted.isEmpty()) {
            return;
        }

        int rangeSquared = CompetitionConfig.getInstance().getBroadcastRange(); // 10 blocks squared

        if (CompetitionConfig.getInstance().broadcastOnlyRods()) {
            // sends it to all players holding ords
            if (actionBar) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (rangeSquared > -1 && !isWithinRange(referencePlayer, player, rangeSquared)) {
                        continue;
                    }
                    if (player.getInventory().getItemInMainHand().getType().equals(Material.FISHING_ROD) || player.getInventory().getItemInOffHand().getType().equals(Material.FISHING_ROD)) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formatted));
                    }
                }
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (rangeSquared > -1 && !isWithinRange(referencePlayer, player, rangeSquared)) {
                        continue;
                    }
                    if (player.getInventory().getItemInMainHand().getType().equals(Material.FISHING_ROD) || player.getInventory().getItemInOffHand().getType().equals(Material.FISHING_ROD)) {
                        player.sendMessage(formatted);
                    }
                }
            }
            // sends it to everyone
        } else {
            if (actionBar) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (rangeSquared > -1 && !isWithinRange(referencePlayer, player, rangeSquared)) {
                        continue;
                    }
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formatted));
                }
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (rangeSquared > -1 && !isWithinRange(referencePlayer, player, rangeSquared)) {
                        continue;
                    }
                    player.sendMessage(formatted);
                }
            }
        }
    }

    private static boolean isWithinRange(Player player1, Player player2, int rangeSquared) {
        return player1.getWorld() == player2.getWorld() && player1.getLocation().distanceSquared(player2.getLocation()) <= rangeSquared;
    }

    /**
     * Determines whether the bait has the emf nbt tag "bait:", this can be used to decide whether this is a bait that
     * can be applied to a rod or not.
     *
     * @param item The item being considered.
     * @return Whether this ItemStack is a bait.
     */
    public static boolean isBaitObject(ItemStack item) {
        if (item.getItemMeta() != null) {
            return NbtUtils.hasKey(new NBTItem(item), NbtUtils.Keys.EMF_BAIT);
        }

        return false;
    }
}
