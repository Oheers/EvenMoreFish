package com.oheers.fish;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.rarities.Rarity;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FishUtils {

    // checks for the "emf-fish-name" nbt tag, to determine if this ItemStack is a fish or not.
    public static boolean isFish(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return false;
        }

        return NbtUtils.hasKey(item, NbtKeys.EMF_FISH_NAME);
    }

    public static boolean isFish(Skull skull) {
        if (skull == null) {
            return false;
        }

        return NbtUtils.hasKey(skull, NbtKeys.EMF_FISH_NAME);
    }

    public static Fish getFish(ItemStack item) {
        // all appropriate null checks can be safely assumed to have passed to get to a point where we're running this method.

        String nameString = NbtUtils.getString(item, NbtKeys.EMF_FISH_NAME);
        String playerString = NbtUtils.getString(item, NbtKeys.EMF_FISH_PLAYER);
        String rarityString = NbtUtils.getString(item, NbtKeys.EMF_FISH_RARITY);
        Float lengthFloat = NbtUtils.getFloat(item, NbtKeys.EMF_FISH_LENGTH);
        Integer randomIndex = NbtUtils.getInteger(item, NbtKeys.EMF_FISH_RANDOM_INDEX);

        if (nameString == null || rarityString == null) {
            return null; //throw new InvalidFishException("NBT Error");
        }


        // Get the rarity
        Rarity rarity = FishManager.getInstance().getRarity(rarityString);

        if (rarity == null) {
            return null;
        }

        // setting the correct length so it's an exact replica.
        try {
            Fish fish = new Fish(rarity, nameString);
            if (randomIndex != null) {
                fish.getFactory().setType(randomIndex);
            }
            fish.setLength(lengthFloat);
            try {
                if (playerString != null) {
                    fish.setFisherman(UUID.fromString(playerString));
                }
            } catch (Exception ex) {
                fish.setFisherman(null);
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
        final String nameString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_NAME).toString()));
        final String playerString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_PLAYER).toString()));
        final String rarityString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_RARITY).toString()));
        final Float lengthFloat = NBT.getPersistentData(skull, nbt -> nbt.getFloat(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_LENGTH).toString()));
        final Integer randomIndex = NBT.getPersistentData(skull, nbt -> nbt.getInteger(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_RANDOM_INDEX).toString()));

        if (nameString == null || rarityString == null) {
            throw new InvalidFishException("NBT Error");
        }

        // Get the rarity
        Rarity rarity = FishManager.getInstance().getRarity(rarityString);

        if (rarity == null) {
            return null;
        }

        // setting the correct length and randomIndex, so it's an exact replica.
        Fish fish = new Fish(rarity, nameString);
        fish.setLength(lengthFloat);
        if (randomIndex != null) {
            fish.getFactory().setType(randomIndex);
        }
        try {
            if (playerString != null) {
                try {
                    fish.setFisherman(UUID.fromString(playerString));
                } catch (IllegalArgumentException ex) {
                    fish.setFisherman(fisher.getUniqueId());
                }
            } else {
                fish.setFisherman(fisher.getUniqueId());
            }
        } catch (IllegalArgumentException exception) {
            fish.setFisherman(null);
        }

        return fish;
    }

    public static void giveItems(List<ItemStack> items, Player player) {
        if (items.isEmpty()) {
            return;
        }
        // Remove null items
        items = items.stream().filter(Objects::nonNull).toList();
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);
        player.getInventory().addItem(items.toArray(new ItemStack[0]))
                .values()
                .forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
    }

    public static void giveItems(ItemStack[] items, Player player) {
        giveItems(Arrays.asList(items), player);
    }

    public static void giveItem(ItemStack item, Player player) {
        giveItems(List.of(item), player);
    }

    public static boolean checkRegion(Location l, List<String> whitelistedRegions) {
        // if the user has defined a region whitelist
        if (whitelistedRegions.isEmpty()) {
            return true;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            // Creates a query for whether the player is stood in a protected region defined by the user
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
        } else if (Bukkit.getPluginManager().isPluginEnabled("RedProtect")) {
            Region r = RedProtect.get().getAPI().getRegion(l);
            // if the hook is in any RedProtect region
            if (r != null) {
                // if the hook is in a whitelisted region
                return whitelistedRegions.contains(r.getName());
            }
            return false;
        } else {
            // the user has defined a region whitelist but doesn't have a region plugin.
            EvenMoreFish.getInstance().getLogger().warning("Please install WorldGuard or RedProtect to use allowed-regions.");
            return true;
        }
    }

    public static String getRegionName(Location location) {
        if (MainConfig.getInstance().isRegionBoostsEnabled()) {
            Plugin worldGuard = EvenMoreFish.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
            if (worldGuard != null && worldGuard.isEnabled()) {
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionQuery query = container.createQuery();
                ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
                for (ProtectedRegion region : set) {
                    return region.getId(); // Return the first region found
                }
            } else if (EvenMoreFish.getInstance().getServer().getPluginManager().isPluginEnabled("RedProtect")) {
                Region region = RedProtect.get().getAPI().getRegion(location);
                if (region != null) {
                    return region.getName();
                }
            } else {
                EvenMoreFish.getInstance().getLogger().warning("Please install WorldGuard or RedProtect to use region-boosts.");
            }
        }
        return null; // Return null if no region is found or no region plugin is enabled
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

    public static String translateColorCodes(String message) {
        return EvenMoreFish.getAdapter().translateColorCodes(message);
    }

    //gets the item with a custom texture
    public static ItemStack getSkullFromBase64(String base64EncodedString) {
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        UUID headUuid = UUID.randomUUID();
        // 1.20.5+ handling
        if (MinecraftVersion.isNewerThan(MinecraftVersion.MC1_20_R3)) {
            NBT.modifyComponents(skull, nbt -> {
                ReadWriteNBT profileNbt = nbt.getOrCreateCompound("minecraft:profile");
                profileNbt.setUUID("id", headUuid);
                ReadWriteNBT propertiesNbt = profileNbt.getCompoundList("properties").addCompound();
                // This key is required, so we set it to an empty string.
                propertiesNbt.setString("name", "textures");
                propertiesNbt.setString("value", base64EncodedString);
            });
        // 1.20.4 and below handling
        } else {
            NBT.modify(skull, nbt -> {
                ReadWriteNBT skullOwnerCompound = nbt.getOrCreateCompound("SkullOwner");
                skullOwnerCompound.setUUID("Id", headUuid);
                skullOwnerCompound.getOrCreateCompound("Properties")
                        .getCompoundList("textures")
                        .addCompound()
                        .setString("Value", base64EncodedString);
            });
        }
        return skull;
    }

    //gets the item with a custom uuid
    public static ItemStack getSkullFromUUID(UUID uuid) {
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        // 1.20.5+ handling
        if (MinecraftVersion.isNewerThan(MinecraftVersion.MC1_20_R3)) {
            NBT.modifyComponents(skull, nbt -> {
                ReadWriteNBT profileNbt = nbt.getOrCreateCompound("minecraft:profile");
                profileNbt.setUUID("id", uuid);
            });
            // 1.20.4 and below handling
        } else {
            NBT.modify(skull, nbt -> {
                ReadWriteNBT skullOwnerCompound = nbt.getOrCreateCompound("SkullOwner");
                skullOwnerCompound.setUUID("Id", uuid);
            });
        }
        return skull;
    }

    public static String timeFormat(long timeLeft) {
        String returning = "";
        long hours = timeLeft / 3600;
        long minutes = (timeLeft % 3600) / 60;
        long seconds = timeLeft % 60;

        if (hours > 0) {
            AbstractMessage message = ConfigMessage.BAR_HOUR.getMessage();
            message.setVariable("{hour}", String.valueOf(hours));
            returning += message.getLegacyMessage() + " ";
        }

        if (minutes > 0) {
            AbstractMessage message = ConfigMessage.BAR_MINUTE.getMessage();
            message.setVariable("{minute}", String.valueOf(minutes));
            returning += message.getLegacyMessage() + " ";
        }

        // Shows remaining seconds if seconds > 0 or hours and minutes are 0, e.g. "1 minutes and 0 seconds left" and "5 seconds left"
        if (seconds > 0 || (minutes == 0 && hours == 0)) {
            AbstractMessage message = ConfigMessage.BAR_SECOND.getMessage();
            message.setVariable("{second}", String.valueOf(seconds));
            returning += message.getLegacyMessage() + " ";
        }

        return returning.trim();
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

    public static void broadcastFishMessage(AbstractMessage message, Player referencePlayer, boolean actionBar) {

        String formatted = message.getLegacyMessage();
        Competition activeComp = Competition.getCurrentlyActive();

        if (formatted.isEmpty() || activeComp == null) {
            return;
        }

        CompetitionFile activeCompetitionFile = activeComp.getCompetitionFile();

        int rangeSquared = activeCompetitionFile.getBroadcastRange(); // 10 blocks squared

        if (activeCompetitionFile.shouldBroadcastOnlyRods()) {
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
            return NbtUtils.hasKey(item, NbtKeys.EMF_BAIT);
        }

        return false;
    }

    /**
     * Gets the first Character from a given String
     * @param string The String to use.
     * @param defaultChar The default character to use if an exception is thrown.
     * @return The first Character from the String
     */
    public static char getCharFromString(@NotNull String string, char defaultChar) {
        try {
            return string.toCharArray()[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return defaultChar;
        }
    }

    public static Biome getBiome(@NotNull String keyString) {
        // Force lowercase
        keyString = keyString.toLowerCase();
        // If no namespace, assume minecraft
        if (!keyString.contains(":")) {
            keyString = "minecraft:" + keyString;
        }
        // Get the key and check if null
        NamespacedKey key = NamespacedKey.fromString(keyString);
        if (key == null) {
            EvenMoreFish.getInstance().getLogger().severe(keyString + " is not a valid biome.");
            return null;
        }
        // Get the biome and check if null
        Biome biome = Registry.BIOME.get(key);
        if (biome == null) {
            EvenMoreFish.getInstance().getLogger().severe(keyString + " is not a valid biome.");
            return null;
        }
        return biome;
    }

    // TODO cleanup
    public static double getTotalWeight(List<Fish> fishList, double boostRate, List<Fish> boostedFish) {
        double totalWeight = 0;

        for (Fish fish : fishList) {
            // when boostRate is -1, we need to guarantee a fish, so the fishList has already been moderated to only contain
            // boosted fish. The other 2 check that the plugin wants the bait calculations too.
            if (boostRate != -1 && boostedFish != null && boostedFish.contains(fish)) {

                if (fish.getWeight() == 0.0d) totalWeight += (1 * boostRate);
                else
                    totalWeight += fish.getWeight() * boostRate;
            } else {
                if (fish.getWeight() == 0.0d) totalWeight += 1;
                else totalWeight += fish.getWeight();
            }
        }
        return totalWeight;
    }

    public static @Nullable DayOfWeek getDay(@NotNull String day) {
        try {
            return DayOfWeek.valueOf(day.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static @Nullable Integer getInteger(@NotNull String intString) {
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static boolean classExists(@NotNull String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

}
