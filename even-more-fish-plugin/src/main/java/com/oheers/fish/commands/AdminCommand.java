package com.oheers.fish.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.addons.AddonManager;
import com.oheers.fish.api.addons.Addon;
import com.oheers.fish.api.reward.RewardManager;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitNBTManager;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.permissions.AdminPerms;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@CommandAlias("%main")
@Subcommand("admin")
@CommandPermission(AdminPerms.ADMIN)
public class AdminCommand extends BaseCommand {
    @Subcommand("fish")
    @CommandCompletion("@rarities @fish @range:1-64 @players")
    @Description("%desc_admin_fish")
    public void onFish(final CommandSender sender, final Rarity rarity, final Fish fish, @Optional @Default("1") @Conditions("limits:min=1") Integer quantity, @Optional OnlinePlayer player) {
        if (player == null && !(sender instanceof Player)) {
            new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE).broadcast(sender);
            return;
        }

        Player target = getPlayerFromOnlinePlayerAndSender(sender, player);

        fish.init();

        fish.checkFishEvent();

        if (fish.hasFishRewards()) {
            fish.getFishRewards().forEach(fishReward -> fishReward.rewardPlayer(target, target.getLocation()));
        }

        fish.setFisherman(target.getUniqueId());

        final ItemStack fishItem = fish.give(-1);
        fishItem.setAmount(quantity);

        FishUtils.giveItems(Collections.singletonList(fishItem), target);

        Message message = new Message(ConfigMessage.ADMIN_GIVE_PLAYER_FISH);
        message.setPlayer(target);
        message.setFishCaught(fish.getName());
        message.broadcast(sender);
        //give fish to target
    }

    private Player getPlayerFromOnlinePlayerAndSender(final CommandSender sender, final OnlinePlayer player) {
        if (player == null) {
            return (Player) sender;
        }
        return player.player;
    }

    /**
     * We must suppress InnerClassMayBeStatic
     * If this class is static it will not work properly with ACF
     */
    @SuppressWarnings("InnerClassMayBeStatic")
    @Subcommand("list")
    public class ListSubCommand extends BaseCommand {

        @Subcommand("fish")
        @CommandCompletion("@rarities")
        @Description("%desc_list_fish")
        public void onFish(final CommandSender sender, final Rarity rarity) {
            BaseComponent baseComponent = new TextComponent(FishUtils.translateColorCodes(rarity.getColour() + rarity.getDisplayName()) + " ");
            for (Fish fish : EvenMoreFish.getInstance().getFishCollection().get(rarity)) {
                BaseComponent textComponent = new TextComponent(FishUtils.translateColorCodes(rarity.getColour() + "[" + fish.getDisplayName() + rarity.getColour()+ "] "));
                textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("Click to receive fish"))));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin fish " + rarity.getValue() + " " + fish.getName().replace(" ","_")));
                baseComponent.addExtra(textComponent);
            }
            sender.spigot().sendMessage(baseComponent);
        }


        @Subcommand("rarities")
        @Description("%desc_list_rarities")
        public void onRarity(final CommandSender sender) {
            BaseComponent baseComponent = new TextComponent("");
            for (Rarity rarity : EvenMoreFish.getInstance().getFishCollection().keySet()) {
                BaseComponent textComponent = new TextComponent(FishUtils.translateColorCodes(rarity.getColour() + "[" + rarity.getDisplayName() + "] "));
                textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("Click to view " + rarity.getDisplayName() + " fish."))));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin list fish " + rarity.getValue()));
                baseComponent.addExtra(textComponent);
            }
            sender.spigot().sendMessage(baseComponent);
        }

    }

    /**
     * We must suppress InnerClassMayBeStatic
     * If this class is static it will not work properly with ACF
     */
    @SuppressWarnings("InnerClassMayBeStatic")
    @Subcommand("competition")
    public class CompetitionSubCommand extends BaseCommand {

        @Subcommand("start")
        @Description("%desc_competition_start")
        public void onStart(final CommandSender sender,
                            @Default("%duration") @Conditions("limits:min=1") @Optional Integer duration,
                            @Default("LARGEST_FISH") @Optional CompetitionType type,
                            @Default("1") @Conditions("limits:min=1") @Optional Integer amount
        ) {
            if (Competition.isActive()) {
                new Message(ConfigMessage.COMPETITION_ALREADY_RUNNING).broadcast(sender);
                return;
            }


            Competition comp = new Competition(duration, type, new ArrayList<>());

            comp.setCompetitionName("[admin_started]");
            comp.setAdminStarted(true);
            comp.initRewards(null, true);
            comp.initBar(null);
            comp.setNumberNeeded(amount);
            comp.initStartSound(null);

            EvenMoreFish.getInstance().setActiveCompetition(comp);
            comp.begin(true);
        }

        @Subcommand("end")
        @Description("%desc_competition_end")
        public void onEnd(final CommandSender sender) {
            if (Competition.isActive()) {
                EvenMoreFish.getInstance().getActiveCompetition().end(false);
                return;
            }

            new Message(ConfigMessage.NO_COMPETITION_RUNNING).broadcast(sender);
        }

    }

    @Subcommand("nbt-rod")
    @Description("%desc_admin_nbtrod")
    public void onNbtRod(final CommandSender sender, @Optional String playerName) {
        if (!MainConfig.getInstance().requireNBTRod()) {
            new Message(ConfigMessage.ADMIN_NBT_NOT_REQUIRED).broadcast(sender);
            return;
        }

        Player player = null;
        if (playerName != null) {
            player = Bukkit.getPlayer(playerName);
            if (player == null) {
                new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE).broadcast(sender);
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (player == null) {
            new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE).broadcast(sender);
            return;
        }

        FishUtils.giveItems(Collections.singletonList(EvenMoreFish.getInstance().getCustomNBTRod()), player);
        Message giveMessage = new Message(ConfigMessage.ADMIN_NBT_ROD_GIVEN);
        giveMessage.setPlayer(player);
        giveMessage.broadcast(sender);
    }


    @Subcommand("bait")
    @CommandCompletion("@baits @range:1-64 @players")
    @Description("%desc_admin_bait")
    public void onBait(final CommandSender sender, String baitName, @Default("1") @Conditions("limits:min=1,max=64") Integer quantity, @Optional OnlinePlayer player) {
        final String baitId = getBaitIdFromName(baitName);
        final Bait bait = EvenMoreFish.getInstance().getBaits().get(baitId);
        if (baitId == null || bait == null) {
            //could not get bait for some reason.
            return;
        }


        if (player == null) {
            if (!(sender instanceof Player)) {
                new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE).broadcast(sender);
                return;
            }

            ItemStack baitItem = bait.create(Bukkit.getOfflinePlayer(((Player) sender).getUniqueId()));
            baitItem.setAmount(quantity);
            FishUtils.giveItems(Collections.singletonList(baitItem), (Player) sender);
            return;
        }

        ItemStack baitItem = bait.create(player.player);
        baitItem.setAmount(quantity);
        FishUtils.giveItems(Collections.singletonList(baitItem), player.player);
        Message message = new Message(ConfigMessage.ADMIN_GIVE_PLAYER_BAIT);
        message.setPlayer(player.player);
        message.setBait(baitId);
        message.broadcast(sender);
    }

    private String getBaitIdFromName(final String baitName) {
        for (String baitID : EvenMoreFish.getInstance().getBaits().keySet()) {
            if (baitID.equalsIgnoreCase(baitName) || baitID.equalsIgnoreCase(baitName.replace("_", " "))) {
                return baitID;
            }
        }

        return null;
    }

    @Subcommand("clearbaits")
    @Description("%desc_admin_clearbaits")
    public void onClearBaits(final CommandSender sender, @Optional Player player) {
        if (player == null && !(sender instanceof Player)) {
            new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE).broadcast(sender);
            return;
        }

        if (player == null) {
            player = (Player) sender;
        }

        if (player.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD) {
            new Message(ConfigMessage.ADMIN_NOT_HOLDING_ROD).broadcast(player);
            return;
        }

        ItemStack fishingRod = player.getInventory().getItemInMainHand();
        if (!BaitNBTManager.isBaitedRod(fishingRod)) {
            new Message(ConfigMessage.NO_BAITS).broadcast(player);
            return;
        }

        ItemMeta meta = fishingRod.getItemMeta();
        meta.setLore(BaitNBTManager.deleteOldLore(fishingRod));
        fishingRod.setItemMeta(meta);
        Message message = new Message(ConfigMessage.BAITS_CLEARED);
        message.setAmount(Integer.toString(BaitNBTManager.deleteAllBaits(fishingRod)));
        message.broadcast(player);
    }


    @Subcommand("reload")
    @Description("%desc_admin_reload")
    public void onReload(final CommandSender sender) {
        EvenMoreFish.getInstance().reload(sender);
    }


    @Subcommand("addons")
    @Description("%desc_admin_addons")
    public void onAddons(final CommandSender sender) {
        final AddonManager addonManager = EvenMoreFish.getInstance().getAddonManager();
        final String messageFormat = "Addon: %s, Loading: %b";
        final List<String> messageList = new ArrayList<>();
        for (final Map.Entry<String, Addon> entry : addonManager.getAddonMap().entrySet()) {
            final String prefix = entry.getKey();
            messageList.add(String.format(messageFormat, prefix, addonManager.isLoading(prefix)));
        }

        new Message(messageList).broadcast(sender);
    }

    @Subcommand("version")
    @Description("%desc_admin_version")
    public void onVersion(final CommandSender sender) {
        int fishCount = 0;
        for (Rarity r : EvenMoreFish.getInstance().getFishCollection().keySet()) {
            fishCount += EvenMoreFish.getInstance().getFishCollection().get(r).size();
        }
        
        String msgString = Messages.getInstance().getSTDPrefix() + "EvenMoreFish by Oheers " + EvenMoreFish.getInstance().getDescription().getVersion() + "\n" +
                Messages.getInstance().getSTDPrefix() + "Feature Branch: " + getFeatureBranchName() + "\n" +
                Messages.getInstance().getSTDPrefix() + "Feature Build/Date: " + getFeatureBranchBuildOrDate() + "\n" +
                Messages.getInstance().getSTDPrefix() + "MCV: " + Bukkit.getServer().getVersion() + "\n" +
                Messages.getInstance().getSTDPrefix() + "SSV: " + Bukkit.getServer().getBukkitVersion() + "\n" +
                Messages.getInstance().getSTDPrefix() + "Online: " + Bukkit.getServer().getOnlineMode() + "\n" +
                Messages.getInstance().getSTDPrefix() + "Loaded: Rarities(" + EvenMoreFish.getInstance().getFishCollection().size() + ") Fish(" +
                fishCount + ") Baits(" + EvenMoreFish.getInstance().getBaits().size() + ") Competitions(" + EvenMoreFish.getInstance().getCompetitionQueue().getSize() + ")\n" +
                Messages.getInstance().getSTDPrefix();

        msgString += "Database Engine: " + getDatabaseVersion();

        Message msg = new Message(msgString);
        msg.broadcast(sender);
    }

    private String getFeatureBranchName() {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF")) {

            if (inputStream != null) {
                Manifest manifest = new Manifest(inputStream);

                // Access attributes from the manifest file
                return manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            } else {
                return "main";
            }

        } catch (IOException e) {
            return "main";
        }
    }

    private String getFeatureBranchBuildOrDate() {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF")) {

            if (inputStream != null) {
                Manifest manifest = new Manifest(inputStream);

                // Access attributes from the manifest file
                return manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            } else {
                return "";
            }

        } catch (IOException e) {
            return "";
        }
    }

    private String getDatabaseVersion() {
        if (!MainConfig.getInstance().databaseEnabled()) {
            return "None";
        }

        if (EvenMoreFish.getInstance().getDatabaseV3().usingVersionV2()) {
            return "V2";
        }

        return "V3";
    }

    @Subcommand("rewardtypes")
    @Description("%desc_admin_rewardtypes")
    public void onRewardTypes(final CommandSender sender) {
        TextComponent message = new TextComponent(new Message(ConfigMessage.ADMIN_LIST_REWARD_TYPES).getRawMessage());
        ComponentBuilder builder = new ComponentBuilder(message);

        RewardManager.getInstance().getRegisteredRewardTypes().forEach(rewardType -> {
            TextComponent component = new TextComponent(rewardType.getIdentifier());
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new Text(TextComponent.fromLegacyText(
                            "Author: " + rewardType.getAuthor() + "\n" +
                                    "Registered Plugin: " + rewardType.getPlugin().getName()
                    ))
            ));
            builder.append(component).append(", ");
        });
        sender.spigot().sendMessage(builder.create());
    }

    @Subcommand("migrate")
    @Description("%desc_admin_migrate")
    @CommandPermission(AdminPerms.MIGRATE)
    public void onMigrate(final CommandSender sender) {
        if (!MainConfig.getInstance().databaseEnabled()) {
            new Message("You cannot run migrations when the database is disabled. Please set database.enabled: true. And restart the server.").broadcast(sender);
            return;
        }
        EvenMoreFish.getScheduler().runTaskAsynchronously(() -> EvenMoreFish.getInstance().getDatabaseV3().migrateLegacy(sender));
    }
}
