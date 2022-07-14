package com.oheers.fish;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
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
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FishUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");
    private static final char COLOR_CHAR = '\u00A7';

    /* checks for the "emf-fish-length" nbt tag, to determine if this itemstack is a fish or not.
     * we only need to check for the length since they're all added in a batch if it's an EMF fish */
    public static boolean isFish(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            return NbtUtils.hasKey(new NBTItem(item), NbtUtils.Keys.EMF_FISH_LENGTH);
        }

        return false;
    }

    public static boolean isFish(Skull skull) {
        if (skull != null) {
            return NbtUtils.hasKey(new NBTTileEntity(skull), NbtUtils.Keys.EMF_FISH_LENGTH);
        }

        return false;
    }

    public static Fish getFish(ItemStack item) {
        // all appropriate null checks can be safely assumed to have passed to get to a point where we're running this method.
        NBTItem nbtItem = new NBTItem(item);

        String nameString = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_NAME);
        String playerString = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_PLAYER);
        String rarityString = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_RARITY);
        Float lengthFloat = NbtUtils.getFloat(nbtItem, NbtUtils.Keys.EMF_FISH_LENGTH);
        Integer randomIndex = NbtUtils.getInteger(nbtItem, NbtUtils.Keys.EMF_FISH_RANDOM_INDEX);

        if (nameString == null || rarityString == null || lengthFloat == null)
            return null; //throw new InvalidFishException("NBT Error");


        // Generating an empty rarity
        Rarity rarity = null;
        // Hunting through the fish collection and creating a rarity that matches the fish's nbt
        for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
            if (r.getValue().equals(rarityString)) {
                rarity = new Rarity(r.getValue(), r.getColour(), r.getWeight(), r.getAnnounce(), r.overridenLore);
            }
        }

        // setting the correct length so it's an exact replica.
        try {
            Fish fish = new Fish(rarity, nameString);
            if (randomIndex != null) fish.getFactory().setType(randomIndex);
            fish.setLength(lengthFloat);
            if (playerString != null) fish.setFisherman(UUID.fromString(playerString));

            return fish;
        } catch (InvalidFishException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, "Could not create fish from an ItemStack with rarity " + rarityString + " and name " + nameString + ". You may have" +
                    "deleted the fish since this fish was caught.");
        }

        return null;
    }

    public static Fish getFish(Skull skull, Player fisher) throws InvalidFishException {
        // all appropriate null checks can be safely assumed to have passed to get to a point where we're running this method.
        NBTTileEntity nbtSkull = new NBTTileEntity(skull);

        String nameString = NbtUtils.getString(nbtSkull, NbtUtils.Keys.EMF_FISH_NAME);
        String playerString = NbtUtils.getString(nbtSkull, NbtUtils.Keys.EMF_FISH_PLAYER);
        String rarityString = NbtUtils.getString(nbtSkull, NbtUtils.Keys.EMF_FISH_RARITY);
        Float lengthFloat = NbtUtils.getFloat(nbtSkull, NbtUtils.Keys.EMF_FISH_LENGTH);
        Integer randomIndex = NbtUtils.getInteger(nbtSkull, NbtUtils.Keys.EMF_FISH_RANDOM_INDEX);

        if (nameString == null || rarityString == null || lengthFloat == null)
            throw new InvalidFishException("NBT Error");

        // Generating an empty rarity
        Rarity rarity = null;
        // Hunting through the fish collection and creating a rarity that matches the fish's nbt
        for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
            if (r.getValue().equals(rarityString)) {
                rarity = new Rarity(r.getValue(), r.getColour(), r.getWeight(), r.getAnnounce(), r.overridenLore);
            }
        }

        // setting the correct length and randomIndex, so it's an exact replica.
        Fish fish = new Fish(rarity, nameString);
        fish.setLength(lengthFloat);
        if (randomIndex != null) {
            fish.getFactory().setType(randomIndex);
        }
        if (playerString != null) {
            fish.setFisherman(UUID.fromString(playerString));
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
                .forEach(item -> new BukkitRunnable() {
                    public void run() {
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                }.runTask(JavaPlugin.getProvidingPlugin(FishUtils.class)));
    }

    public static boolean checkRegion(Location l, List<String> whitelistedRegions) {
        // if there's any region plugin installed
        if (EvenMoreFish.guardPL == null) {
            return true;
        }
        // if the user has defined a region whitelist
        if (whitelistedRegions.size() == 0) {
            return true;
        }

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
            EvenMoreFish.logger.log(Level.WARNING, "Please install WorldGuard or RedProtect to enable region-specific fishing.");
            return true;
        }
    }

    public static boolean checkWorld(Location l) {
        // if the user has defined a world whitelist
        if (!EvenMoreFish.mainConfig.worldWhitelist()) {
            return true;
        }

        // Gets a list of user defined regions
        List<String> whitelistedWorlds = EvenMoreFish.mainConfig.getAllowedWorlds();

        return whitelistedWorlds.contains(l.getWorld().getName());
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
        NBTItem nbtItem = new NBTItem(skull);
        NBTCompound nbtCompound = nbtItem.addCompound("SkullOwner");
        nbtCompound.setString("Id", UUID.randomUUID().toString());

        NBTListCompound texture = nbtCompound.addCompound("Properties").getCompoundList("textures").addCompound();
        texture.setString("Value", base64EncodedString);
        return nbtItem.getItem();
    }

    public static String timeFormat(long timeLeft) {
        String returning = "";
        long hours = timeLeft / 3600;

        if (timeLeft >= 3600) {
            returning += hours + new Message(ConfigMessage.BAR_HOUR).getRawMessage(false, false) + " ";
        }

        if (timeLeft >= 60) {
            returning += ((timeLeft % 3600) / 60) + new Message(ConfigMessage.BAR_MINUTE).getRawMessage(false, false) + " ";
        }

        // Remaining seconds to always show, e.g. "1 minutes and 0 seconds left" and "5 seconds left"
        returning += (timeLeft % 60) + new Message(ConfigMessage.BAR_SECOND).getRawMessage(false, false);
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

    public static void broadcastFishMessage(Message message, boolean actionBar) {
        if (EvenMoreFish.competitionConfig.broadcastOnlyRods()) {
            // sends it to all players holding ords
            if (actionBar) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getInventory().getItemInMainHand().getType().equals(Material.FISHING_ROD) || p.getInventory().getItemInOffHand().getType().equals(Material.FISHING_ROD)) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message.getRawMessage(true, true)));
                    }
                }
            } else {
                String formatted = message.getRawMessage(true, true);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getInventory().getItemInMainHand().getType().equals(Material.FISHING_ROD) || p.getInventory().getItemInOffHand().getType().equals(Material.FISHING_ROD)) {
                        p.sendMessage(formatted);
                    }
                }
            }
            // sends it to everyone
        } else {
            if (actionBar) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message.getRawMessage(true, true)));
                }
            } else {
                message.broadcast(true, true);
            }
        }
    }

    /**
     * Determines whether the bait has the emf nbt tag "bait:", this can be used to decide whether this is a bait that
     * can be applied to a rod or not.
     *
     * @param item The item being considered.
     * @return Whether this ItemStack is a bait.
     */
    public boolean isBaitObject(ItemStack item) {
        if (item.getItemMeta() != null) {
            return NbtUtils.hasKey(new NBTItem(item), NbtUtils.Keys.EMF_BAIT);
        }

        return false;
    }
}
