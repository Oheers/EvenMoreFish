package com.oheers.fish;

import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitNBTManager;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.selling.SellGUI;
import com.oheers.fish.xmas2022.XmasGUI;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandCentre implements TabCompleter, CommandExecutor {

    private static final List<String> empty = new ArrayList<>();
    private static List<String> emfTabs, adminTabs, compTabs, compTypes;
    public EvenMoreFish plugin;

    public CommandCentre(EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    public static void loadTabCompletes() {
        adminTabs = Arrays.asList(
                "bait",
                "clearbaits",
                "competition",
                "fish",
                "nbt-rod",
                "reload",
                "version"
        );

        compTabs = Arrays.asList(
                "start",
                "end"
        );

        emfTabs = new ArrayList<>(Arrays.asList(
                "help",
                "shop",
                "toggle",
                "top",
                "gui")
        );

        if (EvenMoreFish.xmas2022Config.isAvailable()) emfTabs.add("xmas");

        compTypes = Arrays.asList(
                "largest_fish",
                "largest_total",
                "most_fish",
                "random",
                "specific_fish",
                "specific_rarity"
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {

        // Aliases are set in the plugin.yml
        if (cmd.getName().equalsIgnoreCase("evenmorefish")) {
            if (args.length == 0) {
                sender.sendMessage(Help.formGeneralHelp(sender));
            } else {
                control(sender, args);
            }
        }

        return true;
    }

    private void control(CommandSender sender, String[] args) {

        // we've already checked that that args exist
        switch (args[0].toLowerCase()) {
            case "top":
                if (EvenMoreFish.permission.has(sender, "emf.top")) {
                    if (!Competition.isActive()) {
                        new Message(ConfigMessage.NO_COMPETITION_RUNNING).broadcast(sender, true, true);
                    } else {
                        if (sender instanceof Player) {
                            EvenMoreFish.active.sendPlayerLeaderboard((Player) sender);
                        } else if (sender instanceof ConsoleCommandSender) {
                            EvenMoreFish.active.sendConsoleLeaderboard((ConsoleCommandSender) sender);
                        }
                    }
                } else {
                    new Message(ConfigMessage.NO_PERMISSION).broadcast(sender, true, false);
                }
                break;
            case "shop":
                if (sender instanceof Player || args.length > 1) {
                    if (EvenMoreFish.mainConfig.isEconomyEnabled()) {
                        if (EvenMoreFish.permission.has(sender, "emf.shop")) {
                            if (EvenMoreFish.permission.has(sender, "emf.admin") && args.length == 2) {
                                Player p = Bukkit.getPlayer(args[1]);
                                if (p != null) {
                                    new SellGUI(p);
                                    Message message = new Message(ConfigMessage.ADMIN_OPEN_FISH_SHOP);
                                    message.setPlayer(p.getName());
                                    message.broadcast(sender, true, true);
                                } else {
                                    Message message = new Message(ConfigMessage.ADMIN_UNKNOWN_PLAYER);
                                    message.setPlayer(args[1]);
                                    message.broadcast(sender, true, true);
                                }
                            } else {
                                if (sender instanceof Player) {
                                    new SellGUI((Player) sender);
                                }
                            }
                        } else {
                            new Message(ConfigMessage.NO_PERMISSION).broadcast(sender, true, false);
                        }
                    } else {
                        new Message(ConfigMessage.ECONOMY_DISABLED).broadcast(sender, true, false);
                    }
                }
                if (sender instanceof ConsoleCommandSender) {
                    /*new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE).broadcast(sender, true, false);*/
                    if (args.length == 2) {
                        Player p = Bukkit.getPlayer(args[1]);
                        if (p != null) {
                            new SellGUI(p);
                        }
                    }
                }
                break;
            case "toggle":
                if (!(sender instanceof Player)) {
                    new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE).broadcast(sender, true, false);
                    break;
                }

                if (!(EvenMoreFish.permission.has(sender, "emf.toggle"))) {
                    new Message(ConfigMessage.NO_PERMISSION).broadcast(sender, true, false);
                    break;
                }

                if (EvenMoreFish.disabledPlayers.contains(((Player) sender).getUniqueId())) {
                    EvenMoreFish.disabledPlayers.remove(((Player) sender).getUniqueId());
                    new Message(ConfigMessage.TOGGLE_ON).broadcast(sender, true, false);
                } else {
                    EvenMoreFish.disabledPlayers.add(((Player) sender).getUniqueId());
                    new Message(ConfigMessage.TOGGLE_OFF).broadcast(sender, true, false);
                }
                break;
            case "admin":
                if (EvenMoreFish.permission.has(sender, "emf.admin")) {
                    Controls.adminControl(this.plugin, args, sender);
                } else {
                    new Message(ConfigMessage.NO_PERMISSION).broadcast(sender, true, false);
                }
                break;
            case "migrate":
                if (!EvenMoreFish.permission.has(sender, "emf.admin")) {
                    new Message(ConfigMessage.NO_PERMISSION).broadcast(sender, true, false);
                } else {
                    new UniversalRunnable() {

                        @Override
                        public void run() {
                            EvenMoreFish.databaseV3.migrateLegacy(sender);
                        }
                    }.runTaskAsynchronously(JavaPlugin.getProvidingPlugin(CommandCentre.class));
                }
                break;
            case "xmas":
                if (!EvenMoreFish.xmas2022Config.isAvailable()) break;
                if (!EvenMoreFish.permission.has(sender, "emf.xmas")) {
                    new Message(ConfigMessage.NO_PERMISSION).broadcast(sender, true, false);
                } else {
                    new XmasGUI(((Player) sender).getUniqueId()).display((Player) sender);
                }
                break;
            default:
                sender.sendMessage(Help.formGeneralHelp(sender));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        if (args.length > 2 && args[args.length - 1].startsWith("-p:")) {
            if (args[1].equalsIgnoreCase("fish") || args[1].equalsIgnoreCase("bait")) {
                if (EvenMoreFish.permission.has(sender, "emf.admin")) {
                    List<String> playerNames = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        playerNames.add("-p:" + p.getName());
                    }
                    return l(args[args.length - 1], playerNames);
                }
            }
        }

        switch (args.length) {
            case 1:
                if (EvenMoreFish.permission.has(sender, "emf.admin")) {

                    // creates a temp version of tablist where only the qualified completes go through
                    List<String> TEMP_townTabCompletes = l(args[args.length - 1], emfTabs);
                    // if the player is writing "admin" it adds it to the temporary tabcomplete list
                    if ("admin".startsWith(args[args.length - 1].toLowerCase())) {
                        TEMP_townTabCompletes.add("admin");
                    }
                    return TEMP_townTabCompletes;
                } else {
                    return l(args[args.length - 1], emfTabs);
                }
            case 2:
                // checks player has admin perms and has actually used "/emf admin" prior to the 2nd arg
                if (args[0].equalsIgnoreCase("admin") && EvenMoreFish.permission.has(sender, "emf.admin")) {
                    return l(args[args.length - 1], adminTabs);
                } else {
                    return empty;
                }
            case 3:
                if (args[1].equalsIgnoreCase("competition") && args[0].equalsIgnoreCase("admin") && EvenMoreFish.permission.has(sender, "emf.admin")) {
                    return l(args[args.length - 1], compTabs);
                } else if (args[1].equalsIgnoreCase("fish") && args[0].equalsIgnoreCase("admin") && EvenMoreFish.permission.has(sender, "emf.admin")) {
                    List<String> returning = new ArrayList<>();
                    for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                        returning.add(r.getValue().replace(" ", "_"));
                    }

                    return l(args[args.length - 1], returning);
                } else if (args[1].equalsIgnoreCase("bait") && args[0].equalsIgnoreCase("admin") && EvenMoreFish.permission.has(sender, "emf.admin")) {
                    return l(args[args.length - 1], new ArrayList<>(EvenMoreFish.baits.keySet()));
                } else {
                    return empty;
                }
            case 4:
                if (args[1].equalsIgnoreCase("fish") && args[0].equalsIgnoreCase("admin") && EvenMoreFish.permission.has(sender, "emf.admin")) {
                    for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                        if (r.getValue().equalsIgnoreCase(args[2].replace("_", " "))) {
                            List<String> fish = new ArrayList<>();
                            for (Fish f : EvenMoreFish.fishCollection.get(r)) {
                                fish.add(f.getName().replace(" ", "_"));
                            }
                            return l(args[args.length - 1], fish);
                        }
                    }
                    return empty;
                }
                return empty;
            case 5:
                if (EvenMoreFish.permission.has(sender, "emf.admin") && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("competition") && args[2].equalsIgnoreCase("start")) {
                    return l(args[args.length - 1], compTypes);
                } else {
                    return empty;
                }
        }

        return empty;
    }

    // works out how far the player is into the tab and reduces the returned list accordingly
    private List<String> l(String lastArg, List<String> total) {
        List<String> prep = new ArrayList<>();
        for (String s : total) {
            if (s.toLowerCase().startsWith(lastArg.toLowerCase())) {
                prep.add(s);
            }
        }

        return prep;
    }
}

class Controls {

    protected static void adminControl(EvenMoreFish plugin, String[] args, CommandSender sender) {

        // will only proceed after this if at least args[1] exists
        if (args.length == 1) {
            new Message(ConfigMessage.HELP_ADMIN).broadcast(sender, true, false);
            return;
        }

        switch (args[1].toLowerCase()) {

            // bumps the command to another method, if it's a little too complicated it gets bumped to yet another method
            case "competition":
                competitionControl(args, sender);
                break;

            case "fish":
                if (args.length == 3) {
                    // Making Word_Word2 into "Word Word2"
                    StringBuilder args2 = new StringBuilder();
                    for (String word : args[2].split("_")) {
                        args2.append(word).append(" ");
                    }
                    for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                        String rarityName = args2.substring(0, args2.length() - 1);
                        if (rarityName.equalsIgnoreCase(r.getValue())) {
                            ComponentBuilder builder = new ComponentBuilder();

                            if (r.getDisplayName() != null)
                                builder.append(FishUtils.translateHexColorCodes(r.getDisplayName()), ComponentBuilder.FormatRetention.NONE);
                            else
                                builder.append(FishUtils.translateHexColorCodes(r.getColour() + "&l" + r.getValue() + ": "), ComponentBuilder.FormatRetention.NONE);

                            for (Fish fish : EvenMoreFish.fishCollection.get(r)) {
                                if (fish.getDisplayName() != null)
                                    builder.append(FishUtils.translateHexColorCodes(r.getColour() + "[" + fish.getDisplayName() + "] "));
                                else
                                    builder.append(FishUtils.translateHexColorCodes(r.getColour() + "[" + fish.getName() + "] "));

                                builder.event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, TextComponent.fromLegacyText("Click to receive fish"))); // The only element of the hover events basecomponents is the item json
                                builder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin fish " + fish.getRarity().getValue() + " " + fish.getName()));
                            }

                            sender.spigot().sendMessage(builder.create());
                            return;
                        }
                    }
                    ComponentBuilder builder = new ComponentBuilder();
                    for (Rarity r : EvenMoreFish.fishCollection.keySet()) {

                        if (r.getDisplayName() != null) {
                            builder.append(FishUtils.translateHexColorCodes("&r[" + r.getDisplayName() + "] "));
                            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to view " + r.getDisplayName() + " fish.")));
                        } else {
                            builder.append(FishUtils.translateHexColorCodes(r.getColour() + "[" + r.getValue() + "] "));
                            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to view " + r.getValue() + " fish.")));
                        }

                        builder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin fish " + r.getValue()));
                    }
                    sender.spigot().sendMessage(builder.create());
                } else if (args.length >= 4) {
                    Player player = null;
                    int quantity = 1;
                    if (args.length > 4) {
                        for (int section = 3; section < args.length; section++) {
                            if (args[section].startsWith("-p:")) {
                                if ((player = Bukkit.getPlayer(args[section].substring(3))) == null) {
                                    Message message = new Message(ConfigMessage.ADMIN_UNKNOWN_PLAYER);
                                    message.setPlayer(args[section].substring(3));
                                    message.broadcast(sender, true, true);
                                    return;
                                }
                            } else if (args[section].startsWith("-q:")) {
                                try {
                                    quantity = Integer.parseInt(args[section].substring(3));
                                } catch (NumberFormatException exception) {
                                    Message message = new Message(ConfigMessage.ADMIN_NUMBER_FORMAT_ERROR);
                                    message.setAmount(args[section].substring(3));
                                    message.broadcast(sender, true, true);
                                    return;
                                }

                                if (quantity <= 0 || quantity > 64) {
                                    Message message = new Message(ConfigMessage.ADMIN_NUMBER_RANGE_ERROR);
                                    message.setAmount(args[section].substring(3));
                                    message.broadcast(sender, true, true);
                                    return;
                                }
                            }
                        }
                    }

                    if (sender instanceof Player) {
                        // Making Word_Word2 into "Word Word2"
                        StringBuilder args2 = new StringBuilder();
                        for (String word : args[2].split("_")) {
                            args2.append(word).append(" ");
                        }
                        String rarityName = args2.substring(0, args2.length() - 1);

                        StringBuilder args3 = new StringBuilder();
                        for (String word : args[3].split("_")) {
                            args3.append(word).append(" ");
                        }
                        String fishName = args3.substring(0, args3.length() - 1);

                        for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                            if (rarityName.equalsIgnoreCase(r.getValue())) {
                                for (Fish f : EvenMoreFish.fishCollection.get(r)) {
                                    if (fishName.equalsIgnoreCase(f.getName())) {

                                        if (player == null) {
                                            f.setFisherman(((Player) sender).getUniqueId());
                                        } else {
                                            f.setFisherman((player).getUniqueId());
                                        }

                                        f.init();

                                        if (f.getFactory().getMaterial() != Material.AIR) {
                                            ItemStack fish = f.give(-1);
                                            fish.setAmount(quantity);
                                            if (player == null)
                                                FishUtils.giveItems(Collections.singletonList(fish), (Player) sender);
                                            else FishUtils.giveItems(Collections.singletonList(f.give(-1)), player);
                                        }

                                        if (player != null) {
                                            Message message = new Message(ConfigMessage.ADMIN_GIVE_PLAYER_FISH);
                                            message.setPlayer(player.getName());
                                            message.setFishCaught(f.getName());
                                            message.broadcast(sender, true, true);
                                        }

                                        break;

                                    }
                                }
                            }
                        }
                    } else {
                        new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE).broadcast(sender, true, false);
                    }

                } else {
                    BaseComponent baseComponent = new TextComponent("");
                    for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                        BaseComponent tC;
                        if (r.getDisplayName() != null) {
                            tC = new TextComponent(FishUtils.translateHexColorCodes("&r[" + r.getDisplayName() + "] "));
                            tC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to view " + r.getDisplayName() + " fish.")));
                        } else {
                            tC = new TextComponent(FishUtils.translateHexColorCodes(r.getColour() + "[" + r.getValue() + "] "));
                            tC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to view " + r.getValue() + " fish.")));
                        }
                        tC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin fish " + r.getValue()));
                        baseComponent.addExtra(tC);
                    }
                    sender.spigot().sendMessage(baseComponent);
                }

                break;

            case "nbt-rod": {
                if (!EvenMoreFish.mainConfig.requireNBTRod()) {
                    new Message(ConfigMessage.ADMIN_NBT_NOT_REQUIRED).broadcast(sender, true, false);
                    return;
                }
                Player player;
                Message giveMessage;
                if (args.length == 3 && args[2].startsWith("-p:")) {
                    if ((player = Bukkit.getPlayer(args[2].substring(3))) == null) {
                        Message errorMessage = new Message(ConfigMessage.ADMIN_UNKNOWN_PLAYER);
                        errorMessage.setPlayer(args[2].substring(3));
                        errorMessage.broadcast(sender, true, true);
                        return;
                    }
                    giveMessage = new Message(ConfigMessage.ADMIN_NBT_ROD_GIVEN);
                    giveMessage.setPlayer(player.getName());
                } else {
                    if (!(sender instanceof Player)) {
                        Message errorMessage = new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE);
                        errorMessage.broadcast(sender, false, false);
                        return;
                    }
                    player = (Player) sender;
                }

                FishUtils.giveItems(Collections.singletonList(EvenMoreFish.customNBTRod), player);
                giveMessage = new Message(ConfigMessage.ADMIN_NBT_ROD_GIVEN);
                giveMessage.setPlayer(player.getName());
                giveMessage.broadcast(sender, true, true);
                break; }
            case "bait":
                if (args.length >= 3) {

                    // Some baits will probably have spaces in, this sorts out that issue.
                    StringBuilder builtName = new StringBuilder();
                    Player player = null;
                    int quantity = 1;

                    for (int i = 2; i < args.length; i++) {
                        if (args[i].startsWith("-p:")) {
                            if ((player = Bukkit.getPlayer(args[i].substring(3))) == null) {
                                Message message = new Message(ConfigMessage.ADMIN_UNKNOWN_PLAYER);
                                message.setPlayer(args[i].substring(3));
                                message.broadcast(sender, true, true);
                                return;
                            }
                        } else if (args[i].startsWith("-q:")) {
                            try {
                                quantity = Integer.parseInt(args[i].substring(3));
                            } catch (NumberFormatException exception) {
                                Message message = new Message(ConfigMessage.ADMIN_NUMBER_FORMAT_ERROR);
                                message.setAmount(args[i].substring(3));
                                message.broadcast(sender, true, true);
                                return;
                            }

                            if (quantity <= 0 || quantity > 64) {
                                Message message = new Message(ConfigMessage.ADMIN_NUMBER_FORMAT_ERROR);
                                message.setAmount(args[i].substring(3));
                                message.broadcast(sender, true, true);
                                return;
                            }
                        } else {
                            builtName.append(args[i]);
                            if (i != args.length - 1 && !(args[i + 1].startsWith("-p:")) && !(args[i + 1].startsWith("-q:")))
                                builtName.append(" ");
                        }
                    }

                    // Finding the bait and giving it to the (presumably) admin.
                    for (String baitID : EvenMoreFish.baits.keySet()) {
                        if (baitID.equalsIgnoreCase(builtName.toString())) {
                            Bait bait = EvenMoreFish.baits.get(baitID);
                            if (player == null) {
                                if (sender instanceof Player) {
                                    ItemStack baitItem = bait.create(Bukkit.getOfflinePlayer(((Player) sender).getUniqueId()));
                                    baitItem.setAmount(quantity);
                                    FishUtils.giveItems(Collections.singletonList(baitItem), (Player) sender);
                                } else {
                                    new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE).broadcast(sender, true, false);
                                }
                            } else {
                                ItemStack baitItem = bait.create(player);
                                baitItem.setAmount(quantity);
                                FishUtils.giveItems(Collections.singletonList(baitItem), player);
                                Message message = new Message(ConfigMessage.ADMIN_GIVE_PLAYER_BAIT);
                                message.setPlayer(player.getName());
                                message.setBait(baitID);
                                message.broadcast(sender, true, true);
                            }
                        }
                    }
                } else {
                    new Message(ConfigMessage.ADMIN_NO_BAIT_SPECIFIED).broadcast(sender, true, false);
                }

                break;

            case "clearbaits":

                Player player = null;
                for (int i = 2; i < args.length; i++) {
                    if (args[i].startsWith("-p:")) {
                        if ((player = Bukkit.getPlayer(args[i].substring(3))) == null) {
                            Message message = new Message(ConfigMessage.ADMIN_UNKNOWN_PLAYER);
                            message.setPlayer(args[i].substring(3));
                            message.broadcast(sender, true, true);
                            return;
                        }
                    }
                }

                if (player == null && !(sender instanceof Player)) {
                    new Message(ConfigMessage.ADMIN_CANT_BE_CONSOLE).broadcast(sender, true, false);
                    return;
                }

                if (player == null) player = (Player) sender;
                if (player.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD) {
                    new Message(ConfigMessage.ADMIN_NOT_HOLDING_ROD).broadcast(player, true, false);
                    return;
                }

                ItemStack fishingRod = player.getInventory().getItemInMainHand();
                if (BaitNBTManager.isBaitedRod(fishingRod)) {
                    ItemMeta meta = fishingRod.getItemMeta();
                    meta.setLore(BaitNBTManager.deleteOldLore(fishingRod));
                    fishingRod.setItemMeta(meta);
                    Message message = new Message(ConfigMessage.BAITS_CLEARED);
                    message.setAmount(Integer.toString(BaitNBTManager.deleteAllBaits(fishingRod)));
                    message.broadcast(player, true, true);
                } else {
                    new Message(ConfigMessage.NO_BAITS).broadcast(player, true, false);
                }

                break;

            case "reload":

                EvenMoreFish.fishFile.reload();
                EvenMoreFish.raritiesFile.reload();
                EvenMoreFish.baitFile.reload();

                plugin.reload();

                new Message(ConfigMessage.RELOAD_SUCCESS).broadcast(sender, true, false);
                break;

            case "version":
                int fishCount = 0;
                for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                    fishCount += EvenMoreFish.fishCollection.get(r).size();
                }

                String msgString = EvenMoreFish.msgs.getSTDPrefix() + "EvenMoreFish by Oheers " + plugin.getDescription().getVersion() + "\n" +
                        EvenMoreFish.msgs.getSTDPrefix() + "MCV: " + Bukkit.getServer().getVersion() + "\n" +
                        EvenMoreFish.msgs.getSTDPrefix() + "SSV: " + Bukkit.getServer().getBukkitVersion() + "\n" +
                        EvenMoreFish.msgs.getSTDPrefix() + "Online: " + Bukkit.getServer().getOnlineMode() + "\n" +
                        EvenMoreFish.msgs.getSTDPrefix() + "Loaded: Rarities(" + EvenMoreFish.fishCollection.size() + ") Fish(" +
                        fishCount + ") Baits(" + EvenMoreFish.baits.size() + ") Competitions(" + EvenMoreFish.competitionQueue.getSize() + ")\n" +
                        EvenMoreFish.msgs.getSTDPrefix();

                if (EvenMoreFish.mainConfig.databaseEnabled() && EvenMoreFish.mainConfig.doingExperimentalFeatures()) {
                    if (EvenMoreFish.databaseV3.usingVersionV2()) {
                        msgString += "Database Engine: V2";
                    } else {
                        msgString += "Database Engine: V3";
                    }
                } else {
                    msgString += "Database Engine: None";
                }

                Message msg = new Message(msgString);
                msg.broadcast(sender, true, false);
                break;
            default:
                new Message(ConfigMessage.HELP_ADMIN).broadcast(sender, true, false);
        }
    }

    protected static void competitionControl(String[] args, CommandSender player) {
        if (args.length == 2) {
            new Message(ConfigMessage.HELP_COMPETITION).broadcast(player, true, false);
        } else {
            {
                if (args[2].equalsIgnoreCase("start")) {
                    // if the admin has only done /emf admin competition start
                    if (args.length < 4) {
                        startComp(Integer.toString(EvenMoreFish.mainConfig.getCompetitionDuration() * 60), player, CompetitionType.LARGEST_FISH);
                    } else {
                        if (args.length < 5) {
                            startComp(args[3], player, CompetitionType.LARGEST_FISH);
                        } else {
                            try {
                                startComp(args[3], player, CompetitionType.valueOf(args[4].toUpperCase()));
                            } catch (IllegalArgumentException iae) {
                                new Message(ConfigMessage.INVALID_COMPETITION_TYPE).broadcast(player, true, false);
                            }
                        }
                    }
                } else if (args[2].equalsIgnoreCase("end")) {
                    if (Competition.isActive()) {
                        EvenMoreFish.active.end();
                    } else {
                        new Message(ConfigMessage.NO_COMPETITION_RUNNING).broadcast(player, true, true);
                    }
                } else {
                    new Message(ConfigMessage.HELP_COMPETITION).broadcast(player, true, false);
                }
            }
        }
    }

    protected static void startComp(String argsDuration, CommandSender player, CompetitionType type) {
        if (Competition.isActive()) {
            new Message(ConfigMessage.COMPETITION_ALREADY_RUNNING).broadcast(player, true, false);
            return;
        }

        try {
            // converts argsDuration to an integer (throwing exceptions) and starts a competition with that
            int duration = Integer.parseInt(argsDuration);
            // I've just discovered /emf admin competition start -1 causes some funky stuff - so this prevents that.
            if (duration > 0) {
                Competition comp = new Competition(duration, type);

                comp.setCompetitionName("[admin_started]");
                comp.setAdminStarted(true);
                comp.initRewards(null, true);
                comp.initBar(null);
                comp.initGetNumbersNeeded(null);
                comp.initStartSound(null);

                EvenMoreFish.active = comp;
                comp.begin(true);
            } else {
                Message message = new Message(ConfigMessage.ADMIN_NUMBER_FORMAT_ERROR);
                message.setAmount(Integer.toString(duration));
                message.broadcast(player, true, true);
            }
        } catch (NumberFormatException nfe) {
            Message message = new Message(ConfigMessage.ADMIN_NUMBER_FORMAT_ERROR);
            message.setAmount(argsDuration);
            message.broadcast(player, true, true);
        }
    }
}

class Help {

    public static String formGeneralHelp(CommandSender user) {

        //return new Message(ConfigMessage.HELP_GENERAL).getRawMessage(true, false);

        StringBuilder out = new StringBuilder();
        List<String> commands = Arrays.asList(new Message(ConfigMessage.HELP_GENERAL).getRawMessage(true, false).split("\n"));

        String escape = "\n";
        if (EvenMoreFish.permission != null && user != null) {
            for (int i = 0; i < commands.size(); i++) {
                if (i == commands.size() - 1) escape = "";
                if (commands.get(i).contains("/emf admin")) {
                    if (EvenMoreFish.permission.has(user, "emf.admin")) out.append(commands.get(i)).append(escape);
                } else if (commands.get(i).contains("/emf top")) {
                    if (EvenMoreFish.permission.has(user, "emf.top")) out.append(commands.get(i)).append(escape);
                } else if (commands.get(i).contains("/emf shop")) {
                    if (EvenMoreFish.permission.has(user, "emf.shop")) out.append(commands.get(i)).append(escape);
                } else if (commands.get(i).contains("/emf toggle")) {
                    if (EvenMoreFish.permission.has(user, "emf.toggle")) out.append(commands.get(i)).append(escape);
                } else out.append(commands.get(i)).append(escape);
            }
        } else {
            for (int i = 0; i < commands.size(); i++) {
                if (i == commands.size() - 1) escape = "";
                out.append(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getSTDPrefix() + commands.get(i) + escape));
            }
        }

        return out.toString();

    }
}
