package com.oheers.fish.commands;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.addons.AddonManager;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.addons.Addon;
import com.oheers.fish.api.reward.RewardManager;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitManager;
import com.oheers.fish.baits.BaitNBTManager;
import com.oheers.fish.commands.arguments.*;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.utils.ManifestUtil;
import de.tr7zw.changeme.nbtapi.NBT;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.jar.Attributes;

public class AdminCommand {

    private final HelpMessageBuilder helpMessageBuilder = HelpMessageBuilder.create();

    private final CommandAPICommand command;

    public AdminCommand(@NotNull String name) {
        this.command = new CommandAPICommand(name)
                .withPermission(AdminPerms.ADMIN)
                .executes(info -> {
                    sendHelpMessage(info.sender());
                })
                .withSubcommands(
                        getFish(),
                        getList(),
                        getCompetition(),
                        getNbtRod(),
                        getBait(),
                        getClearBaits(),
                        getReload(),
                        getAddons(),
                        getVersion(),
                        getRewardTypes(),
                        getMigrate(),
                        getRawItem(),
                        getHelp(),
                        new AdminDatabaseCommand()
                );
    }

    public CommandAPICommand getCommand() {
        return command;
    }

    private void sendHelpMessage(@NotNull CommandSender sender) {
        helpMessageBuilder.sendMessage(sender);
    }

    private CommandAPICommand getFish() {
        helpMessageBuilder.addUsage(
                "admin fish",
                ConfigMessage.HELP_ADMIN_FISH::getMessage
        );
        return new CommandAPICommand("fish")
                .withArguments(
                        RarityArgument.create(),
                        FishArgument.create(),
                        new IntegerArgument("amount", 1).setOptional(true),
                        ArgumentHelper.getPlayerArgument("target").setOptional(true)
                )
                .executes((sender, arguments) -> {
                    final Fish fish = arguments.getUnchecked("fish");
                    if (fish == null) {
                        return;
                    }
                    final int amount = (Integer) arguments.getOptional("amount").orElse(1);
                    final Player target = (Player) arguments.getOptional("target").orElseGet(() -> {
                        if (!(sender instanceof Player player)) {
                            return null;
                        }
                        return player;
                    });

                    if (target == null) {
                        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
                        return;
                    }

                    fish.init();
                    fish.checkFishEvent();
                    if (fish.hasFishRewards()) {
                        fish.getFishRewards().forEach(reward -> reward.rewardPlayer(target, target.getLocation()));
                    }
                    fish.setFisherman(target.getUniqueId());

                    final ItemStack fishItem = fish.give(-1);
                    fishItem.setAmount(amount);

                    FishUtils.giveItem(fishItem, target);

                    AbstractMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_FISH.getMessage();
                    message.setPlayer(target);
                    message.setFishCaught(fish.getName());
                    message.send(sender);
                });
    }

    private CommandAPICommand getList() {
        return new CommandAPICommand("list")
                .withArguments(
                        new MultiLiteralArgument(
                                "listTarget",
                                "fish", "rarities"
                        ),
                        RarityArgument.create().setOptional(true)
                )
                .executes((sender, args) -> {
                    String listTarget = Objects.requireNonNull(args.getUnchecked("listTarget"));
                    switch (listTarget) {
                        case "fish" -> {
                            final Rarity rarity = args.getUnchecked("rarity");
                            if (rarity == null) {
                                ConfigMessage.RARITY_INVALID.getMessage().send(sender);
                                return;
                            }
                            BaseComponent[] baseComponent = TextComponent.fromLegacyText(FishUtils.translateColorCodes(rarity.getColour() + rarity.getDisplayName()) + " ");
                            for (Fish fish : rarity.getOriginalFishList()) {
                                BaseComponent[] textComponent = TextComponent.fromLegacyText(FishUtils.translateColorCodes(rarity.getColour() + "[" + fish.getDisplayName() + rarity.getColour() + "] "));
                                for (BaseComponent component : textComponent) {
                                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("Click to receive fish"))));
                                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin fish " + rarity.getId() + " " + fish.getName().replace(" ", "_")));
                                    baseComponent[0].addExtra(component);
                                }
                            }
                            sender.spigot().sendMessage(baseComponent);
                        }
                        case "rarities" -> {
                            BaseComponent[] baseComponent = TextComponent.fromLegacyText("");
                            for (Rarity rarity : FishManager.getInstance().getRarityMap().values()) {
                                BaseComponent[] textComponent = TextComponent.fromLegacyText(FishUtils.translateColorCodes(rarity.getColour() + "[" + rarity.getDisplayName() + "] "));
                                for (BaseComponent component : textComponent) {
                                    component.setHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            new Text(TextComponent.fromLegacyText("Click to view " + rarity.getDisplayName() + " fish."))
                                    ));
                                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin list fish " + rarity.getId()));
                                    baseComponent[0].addExtra(component);
                                }
                            }
                            sender.spigot().sendMessage(baseComponent);
                        }
                    }
                });
    }

    private CommandAPICommand getNbtRod() {
        helpMessageBuilder.addUsage(
                "admin nbt-rod",
                ConfigMessage.HELP_ADMIN_NBTROD::getMessage
        );
        return new CommandAPICommand("nbt-rod")
                .withArguments(
                        ArgumentHelper.getPlayerArgument("target").setOptional(true)
                )
                .executes(((sender, args) -> {
                    if (!MainConfig.getInstance().requireNBTRod()) {
                        ConfigMessage.ADMIN_NBT_NOT_REQUIRED.getMessage().send(sender);
                        return;
                    }
                    final Player player = (Player) args.getOptional("target").orElseGet(() -> {
                        if (sender instanceof Player p) {
                            return p;
                        }
                        return null;
                    });

                    if (player == null) {
                        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
                        return;
                    }

                    FishUtils.giveItems(Collections.singletonList(EvenMoreFish.getInstance().getCustomNBTRod()), player);
                    AbstractMessage giveMessage = ConfigMessage.ADMIN_NBT_ROD_GIVEN.getMessage();
                    giveMessage.setPlayer(player);
                    giveMessage.send(sender);
                }));
    }

    private CommandAPICommand getBait() {
        helpMessageBuilder.addUsage(
                "admin bait",
                ConfigMessage.HELP_ADMIN_BAIT::getMessage
        );
        return new CommandAPICommand("bait")
                .withArguments(
                        BaitArgument.create(),
                        new IntegerArgument("quantity", 1).setOptional(true),
                        ArgumentHelper.getPlayerArgument("target").setOptional(true)
                )
                .executes((sender, args) -> {
                    final Bait bait = Objects.requireNonNull(args.getUnchecked("bait"));
                    final int quantity = (int) args.getOptional("quantity").orElse(1);
                    final Player target = (Player) args.getOptional("target").orElseGet(() -> {
                        if (sender instanceof Player p) {
                            return p;
                        }
                        return null;
                    });

                    if (target == null) {
                        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
                        return;
                    }

                    ItemStack baitItem = bait.create(target);
                    baitItem.setAmount(quantity);
                    FishUtils.giveItems(List.of(baitItem), target);
                    AbstractMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_BAIT.getMessage();
                    message.setPlayer(target);
                    message.setBait(bait.getName());
                    message.send(sender);
                });
    }

    private CommandAPICommand getClearBaits() {
        helpMessageBuilder.addUsage(
                "admin clearbaits",
                ConfigMessage.HELP_ADMIN_CLEARBAITS::getMessage
        );
        return new CommandAPICommand("clearbaits")
                .withArguments(
                        ArgumentHelper.getPlayerArgument("target").setOptional(true)
                )
                .executes(((sender, args) -> {
                    final Player player = (Player) args.getOptional("target").orElseGet(() -> {
                        if (sender instanceof Player p) {
                            return p;
                        }
                        return null;
                    });

                    if (player == null) {
                        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
                        return;
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
                }));
    }

    private CommandAPICommand getReload() {
        helpMessageBuilder.addUsage(
                "admin reload",
                ConfigMessage.HELP_ADMIN_RELOAD::getMessage
        );
        return new CommandAPICommand("reload")
                .executes(info -> {
                    EvenMoreFish.getInstance().reload(info.sender());
                });
    }

    private CommandAPICommand getAddons() {
        helpMessageBuilder.addUsage(
                "admin addons",
                ConfigMessage.HELP_ADMIN_ADDONS::getMessage
        );
        return new CommandAPICommand("addons")
                .withFullDescription(ConfigMessage.HELP_ADMIN_ADDONS.getMessage().getPlainTextMessage())
                .executes(info -> {
                    final AddonManager addonManager = EvenMoreFish.getInstance().getAddonManager();
                    final String messageFormat = "Addon: %s, Loading: %b, Version: %s";
                    final List<String> messageList = new ArrayList<>();
                    for (final Map.Entry<String, Addon> entry : addonManager.getAddonMap().entrySet()) {
                        final String prefix = entry.getKey();
                        messageList.add(String.format(messageFormat, prefix, addonManager.isLoading(prefix), entry.getValue()));
                    }

                    EvenMoreFish.getAdapter().createMessage(StringUtils.join(messageList, "\n")).send(info.sender());
                });
    }

    private CommandAPICommand getVersion() {
        helpMessageBuilder.addUsage(
            "admin version",
            ConfigMessage.HELP_ADMIN_VERSION::getMessage
        );
        return new CommandAPICommand("version")
            .executes(info -> {
                int fishCount = FishManager.getInstance().getRarityMap().values().stream()
                    .mapToInt(rarity -> rarity.getFishList().size())
                    .sum();

                final String msgString =
                    """
                    {prefix} EvenMoreFish by Oheers {version}\s
                    {prefix} Feature Branch: {branch}\s
                    {prefix} Feature Build/Date: {build-date}\s
                    {prefix} MCV: {mcv}\s
                    {prefix} SSV: {ssv}\s
                    {prefix} Online: {online}\s
                    {prefix} Loaded Rarities({rarities}) Fish({fish}) Baits({baits}) Competitions({competitions})\s
                    {prefix} Database Engine: {engine}\s
                    {prefix} Database Type: {type}\s
                    """
                        .replace("{prefix}", Messages.getInstance().getSTDPrefix())
                        .replace("{version}", EvenMoreFish.getInstance().getDescription().getVersion())
                        .replace("{branch}", getFeatureBranchName())
                        .replace("{build-date}", getFeatureBranchBuildOrDate())
                        .replace("{mcv}", Bukkit.getServer().getVersion())
                        .replace("{ssv}", Bukkit.getServer().getBukkitVersion())
                        .replace("{online}", String.valueOf(Bukkit.getServer().getOnlineMode()))
                        .replace("{rarities}", String.valueOf(FishManager.getInstance().getRarityMap().size()))
                        .replace("{fish}", String.valueOf(fishCount))
                        .replace("{baits}", String.valueOf(BaitManager.getInstance().getBaitMap().size()))
                        .replace("{competitions}", String.valueOf(EvenMoreFish.getInstance().getCompetitionQueue().getSize()))
                        .replace("{engine}", EvenMoreFish.getInstance().getDatabase().getDatabaseVersion())
                        .replace("{type}", EvenMoreFish.getInstance().getDatabase().getType());


                AbstractMessage msg = EvenMoreFish.getAdapter().createMessage(msgString);
                msg.send(info.sender());
            });
    }

    private String getFeatureBranchName() {
        return ManifestUtil.getAttributeFromManifest(Attributes.Name.IMPLEMENTATION_TITLE.toString(), "main");
    }

    private String getFeatureBranchBuildOrDate() {
        return ManifestUtil.getAttributeFromManifest(Attributes.Name.IMPLEMENTATION_VERSION.toString(), "");
    }

    private CommandAPICommand getRewardTypes() {
        helpMessageBuilder.addUsage(
                "admin rewardtypes",
                ConfigMessage.HELP_ADMIN_REWARDTYPES::getMessage
        );
        return new CommandAPICommand("rewardtypes")
                .executes(info -> {
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
                    info.sender().spigot().sendMessage(builder.create());
                });
    }

    private CommandAPICommand getMigrate() {
        helpMessageBuilder.addUsage(
                "admin migrate",
                ConfigMessage.HELP_ADMIN_MIGRATE::getMessage
        );
        return new CommandAPICommand("migrate")
                .executes(info -> {
                    if (!MainConfig.getInstance().databaseEnabled()) {
                        EvenMoreFish.getAdapter()
                                .createMessage("You cannot run migrations when the database is disabled. Please set database.enabled: true. And restart the server.")
                                .send(info.sender());
                        return;
                    }
                    EvenMoreFish.getScheduler().runTaskAsynchronously(() -> EvenMoreFish.getInstance().getDatabase().getMigrationManager().migrateLegacy(info.sender()));
                });
    }

    private CommandAPICommand getRawItem() {
        helpMessageBuilder.addUsage(
                "admin rawItem",
                ConfigMessage.HELP_ADMIN_RAWITEM::getMessage
        );
        return new CommandAPICommand("rawItem")
                .executesPlayer(info -> {
                    ItemStack handItem = info.sender().getInventory().getItemInMainHand();
                    String handItemNbt = NBT.itemStackToNBT(handItem).toString();

                    // Ensure the handItemNbt is escaped for use in YAML
                    // This could be slightly inefficient, but it is the only way I can currently think of.
                    YamlDocument document = new ConfigBase().getConfig();
                    document.set("rawItem", handItemNbt);
                    handItemNbt = document.dump().replaceFirst("rawItem: ", "");

                    TextComponent component = new TextComponent(handItemNbt);
                    component.setHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("Click to copy to clipboard."))
                    ));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, handItemNbt));
                    info.sender().spigot().sendMessage(component);
                });

    }

    private CommandAPICommand getHelp() {
        helpMessageBuilder.addUsage(
                "admin help",
                ConfigMessage.HELP_GENERAL_HELP::getMessage
        );
        return new CommandAPICommand("help")
                .executes(info -> {
                    sendHelpMessage(info.sender());
                });
    }

    // COMPETITION BRANCH

    private CommandAPICommand getCompetition() {
        helpMessageBuilder.addUsage(
                "admin competition",
                ConfigMessage.HELP_ADMIN_COMPETITION::getMessage
        );
        return new CommandAPICommand("competition")
                .withSubcommands(
                        getCompetitionStart(),
                        getCompetitionEnd(),
                        getCompetitionTest()
                );
    }

    private CommandAPICommand getCompetitionStart() {
        return new CommandAPICommand("start")
                .withArguments(
                        // StringArgument containing all loaded competition ids
                        ArgumentHelper.getAsyncStringsArgument(
                                "competitionId",
                                info -> EvenMoreFish.getInstance().getCompetitionQueue().getFileMap().keySet().toArray(String[]::new)
                        ),
                        new IntegerArgument("duration", 1).setOptional(true)
                )
                .executes((sender, arguments) -> {
                    final String id = Objects.requireNonNull(arguments.getUnchecked("competitionId"));
                    final Integer duration = arguments.getUnchecked("duration");
                    if (Competition.isActive()) {
                        ConfigMessage.COMPETITION_ALREADY_RUNNING.getMessage().send(sender);
                        return;
                    }
                    CompetitionFile file = EvenMoreFish.getInstance().getCompetitionQueue().getFileMap().get(id);
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
                });
    }

    private CommandAPICommand getCompetitionEnd() {
        return new CommandAPICommand("end")
                .executes(info -> {
                    Competition active = Competition.getCurrentlyActive();
                    if (active != null) {
                        active.end(false);
                        return;
                    }
                    ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(info.sender());
                });
    }

    private CommandAPICommand getCompetitionTest() {
        return new CommandAPICommand("test")
                .withArguments(
                        new IntegerArgument("duration", 1).setOptional(true),
                        CompetitionTypeArgument.create().setOptional(true)
                )
                .executes((sender, args) -> {
                    if (Competition.isActive()) {
                        ConfigMessage.COMPETITION_ALREADY_RUNNING.getMessage().send(sender);
                        return;
                    }
                    final int duration = (int) args.getOptional("duration").orElse(1);
                    final CompetitionType type = (CompetitionType) args.getOptional("competitionType").orElse(CompetitionType.LARGEST_FISH);
                    CompetitionFile file = new CompetitionFile("adminTest", type, duration);
                    Competition competition = new Competition(file);
                    competition.setAdminStarted(true);
                    competition.begin();
                });
    }

}
