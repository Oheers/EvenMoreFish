package com.oheers.fish.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.addons.AddonManager;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.addons.Addon;
import com.oheers.fish.api.reward.RewardManager;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitManager;
import com.oheers.fish.baits.BaitNBTManager;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.permissions.AdminPerms;
import de.tr7zw.changeme.nbtapi.NBT;
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
            ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
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

        AbstractMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_FISH.getMessage();
        message.setPlayer(target);
        message.setFishCaught(fish.getName());
        message.send(sender);
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
            BaseComponent[] baseComponent = TextComponent.fromLegacyText(FishUtils.translateColorCodes(rarity.getColour() + rarity.getDisplayName()) + " ");
            for (Fish fish : rarity.getFishList()) {
                BaseComponent[] textComponent = TextComponent.fromLegacyText(FishUtils.translateColorCodes(rarity.getColour() + "[" + fish.getDisplayName() + rarity.getColour() + "] "));
                for (BaseComponent component : textComponent) {
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("Click to receive fish"))));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin fish " + rarity.getId() + " " + fish.getName().replace(" ", "_")));
                    baseComponent[0].addExtra(component);
                }
            }
            sender.spigot().sendMessage(baseComponent);
        }

        @Subcommand("rarities")
        @Description("%desc_list_rarities")
        public void onRarity(final CommandSender sender) {
            BaseComponent[] baseComponent = TextComponent.fromLegacyText("");
            for (Rarity rarity : FishManager.getInstance().getRarityMap().values()) {
                BaseComponent[] textComponent = TextComponent.fromLegacyText(FishUtils.translateColorCodes(rarity.getColour() + "[" + rarity.getDisplayName() + "] "));
                for (BaseComponent component : textComponent) {
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("Click to view " + rarity.getDisplayName() + " fish."))));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin list fish " + rarity.getId()));
                    baseComponent[0].addExtra(component);
                }
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
        @CommandCompletion("@competitionId")
        @Description("%desc_competition_start")
        public void onStart(final CommandSender sender, final String competitionId, @Optional @Conditions("limits:min=1") Integer duration) {
            if (Competition.isActive()) {
                ConfigMessage.COMPETITION_ALREADY_RUNNING.getMessage().send(sender);
                return;
            }
            CompetitionFile file = EvenMoreFish.getInstance().getCompetitionQueue().getFileMap().get(competitionId);
            if (file == null) {
                ConfigMessage.INVALID_COMPETITION_ID.getMessage().send(sender);
                return;
            }
            Competition competition = new Competition(file);
            competition.setAdminStarted(true);
            if (duration != null) {
                competition.setMaxDuration(duration);
            }
            competition.begin();
        }

        @Subcommand("test")
        public void onTest(final CommandSender sender,
                           @Default("%duration") @Conditions("limits:min=1") Integer duration,
                           @Default("LARGEST_FISH") CompetitionType type
        ) {
            if (Competition.isActive()) {
                ConfigMessage.COMPETITION_ALREADY_RUNNING.getMessage().send(sender);
                return;
            }
            CompetitionFile file = new CompetitionFile("adminTest", type, duration);
            Competition competition = new Competition(file);
            competition.setAdminStarted(true);
            competition.begin();
        }

        @Subcommand("end")
        @Description("%desc_competition_end")
        public void onEnd(final CommandSender sender) {
            Competition active = Competition.getCurrentlyActive();
            if (active != null) {
                active.end(false);
                return;
            }

            ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(sender);
        }

    }

    @Subcommand("nbt-rod")
    @Description("%desc_admin_nbtrod")
    @CommandCompletion("@players")
    public void onNbtRod(final CommandSender sender, @Optional OnlinePlayer playerName) {
        if (!MainConfig.getInstance().requireNBTRod()) {
            ConfigMessage.ADMIN_NBT_NOT_REQUIRED.getMessage().send(sender);
            return;
        }

        Player player = null;
        if (playerName != null) {
            player = playerName.getPlayer();
        } else if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (player == null) {
            ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
            return;
        }

        FishUtils.giveItems(Collections.singletonList(EvenMoreFish.getInstance().getCustomNBTRod()), player);
        AbstractMessage giveMessage = ConfigMessage.ADMIN_NBT_ROD_GIVEN.getMessage();
        giveMessage.setPlayer(player);
        giveMessage.send(sender);
    }

    @Subcommand("bait")
    @CommandCompletion("@baits @range:1-64 @players")
    @Description("%desc_admin_bait")
    public void onBait(final CommandSender sender, String baitName, @Default("1") @Conditions("limits:min=1,max=64") Integer quantity, @Optional OnlinePlayer player) {
        final String baitId = getBaitIdFromName(baitName);
        final Bait bait = BaitManager.getInstance().getBaitMap().get(baitId);
        if (baitId == null || bait == null) {
            //could not get bait for some reason.
            return;
        }


        if (player == null) {
            if (!(sender instanceof Player)) {
                ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
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
        AbstractMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_BAIT.getMessage();
        message.setPlayer(player.player);
        message.setBait(baitId);
        message.send(sender);
    }

    private String getBaitIdFromName(final String baitName) {
        for (String baitID : BaitManager.getInstance().getBaitMap().keySet()) {
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
            ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
            return;
        }

        if (player == null) {
            player = (Player) sender;
        }

        if (player.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD) {
            ConfigMessage.ADMIN_NOT_HOLDING_ROD.getMessage().send(player);
            return;
        }

        ItemStack fishingRod = player.getInventory().getItemInMainHand();
        if (!BaitNBTManager.isBaitedRod(fishingRod)) {
            ConfigMessage.NO_BAITS.getMessage().send(player);
            return;
        }

        int totalDeleted = BaitNBTManager.deleteAllBaits(fishingRod);
        if (totalDeleted > 0) {
            FishUtils.editMeta(fishingRod, meta -> meta.setLore(BaitNBTManager.deleteOldLore(fishingRod)));
        }

        AbstractMessage message = ConfigMessage.BAITS_CLEARED.getMessage();
        message.setAmount(Integer.toString(totalDeleted));
        message.send(player);
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

        EvenMoreFish.getAdapter().createMessage(messageList).send(sender);
    }

    @Subcommand("version")
    @Description("%desc_admin_version")
    public void onVersion(final CommandSender sender) {
        int fishCount = 0;

        for (Rarity rarity : FishManager.getInstance().getRarityMap().values()) {
            fishCount += rarity.getFishList().size();
        }
        
        String msgString = Messages.getInstance().getSTDPrefix() + "EvenMoreFish by Oheers " + EvenMoreFish.getInstance().getDescription().getVersion() + "\n" +
                Messages.getInstance().getSTDPrefix() + "Feature Branch: " + getFeatureBranchName() + "\n" +
                Messages.getInstance().getSTDPrefix() + "Feature Build/Date: " + getFeatureBranchBuildOrDate() + "\n" +
                Messages.getInstance().getSTDPrefix() + "MCV: " + Bukkit.getServer().getVersion() + "\n" +
                Messages.getInstance().getSTDPrefix() + "SSV: " + Bukkit.getServer().getBukkitVersion() + "\n" +
                Messages.getInstance().getSTDPrefix() + "Online: " + Bukkit.getServer().getOnlineMode() + "\n" +
                Messages.getInstance().getSTDPrefix() + "Loaded: Rarities(" + FishManager.getInstance().getRarityMap().size() + ") Fish(" +
                fishCount + ") Baits(" + BaitManager.getInstance().getBaitMap().size() + ") Competitions(" + EvenMoreFish.getInstance().getCompetitionQueue().getSize() + ")\n" +
                Messages.getInstance().getSTDPrefix();

        msgString += "Database Engine: " + getDatabaseVersion();

        AbstractMessage msg = EvenMoreFish.getAdapter().createMessage(msgString);
        msg.send(sender);
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
        TextComponent message = new TextComponent(ConfigMessage.ADMIN_LIST_REWARD_TYPES.getMessage().getLegacyMessage());
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
            EvenMoreFish.getAdapter().createMessage("You cannot run migrations when the database is disabled. Please set database.enabled: true. And restart the server.").send(sender);
            return;
        }
        EvenMoreFish.getScheduler().runTaskAsynchronously(() -> EvenMoreFish.getInstance().getDatabaseV3().migrateLegacy(sender));
    }

    @Subcommand("rawItem")
    @Description("Outputs this item's raw NBT form")
    public void onRawItem(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
            return;
        }
        ItemStack handItem = player.getInventory().getItemInMainHand();
        String handItemNbt = NBT.itemStackToNBT(handItem).toString();
        TextComponent component = new TextComponent(handItemNbt);
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("Click to copy to clipboard."))
        ));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, handItemNbt));
        player.spigot().sendMessage(component);
    }

}
