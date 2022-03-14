package com.oheers.fish;

import com.oheers.fish.baits.Bait;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.selling.SellGUI;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandCentre implements TabCompleter, CommandExecutor {

    private static final List<String> empty = new ArrayList<>();

    public EvenMoreFish plugin;

    public CommandCentre(EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

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
                        sender.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.competitionNotRunning()));
                    } else {
                        EvenMoreFish.active.sendLeaderboard((Player) sender);
                    }
                } else {
                    sender.sendMessage(new Message().setMSG(EvenMoreFish.msgs.getNoPermission()).setReceiver((Player) sender).toString());
                }
                break;
            case "shop":
                if (sender instanceof Player) {
                    if (EvenMoreFish.mainConfig.isEconomyEnabled()) {
                        if (EvenMoreFish.permission.has(sender, "emf.shop")) {
                            new SellGUI((Player) sender);
                        } else {
                            sender.sendMessage(new Message().setMSG(EvenMoreFish.msgs.getNoPermission()).setReceiver((Player) sender).toString());
                        }
                    } else {
                        sender.sendMessage(new Message().setMSG(EvenMoreFish.msgs.economyDisabled()).setReceiver((Player) sender).toString());
                    }
                } else {
                    EvenMoreFish.msgs.disabledInConsole();
                }
                break;
            case "admin":
                if (EvenMoreFish.permission.has(sender, "emf.admin")) {
                    Controls.adminControl(this.plugin, args, sender);
                } else {
                    Message msg = new Message().setMSG(EvenMoreFish.msgs.getNoPermission());
                    if (sender instanceof Player) msg.setReceiver((Player) sender);
                    sender.sendMessage(msg.toString());
                }
                break;
            default:
                sender.sendMessage(Help.formGeneralHelp(sender));
        }
    }

    private static List<String> emfTabs, adminTabs, compTabs, compTypes;

    public static void loadTabCompletes() {
        adminTabs = Arrays.asList(
                "bait",
                "competition",
                "fish",
                "reload",
                "version"
        );

        compTabs = Arrays.asList(
                "start",
                "end"
        );

        emfTabs = Arrays.asList(
                "help",
                "shop",
                "top"
        );

        compTypes = Arrays.asList(
                "largest_fish",
                "most_fish",
                "random",
                "specific_fish"
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {

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
                            returning.add(r.getValue());
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
                            if (r.getValue().equalsIgnoreCase(args[2])) {
                                List<String> fish = new ArrayList<>();
                                for (Fish f : EvenMoreFish.fishCollection.get(r)) {
                                    fish.add(f.getName());
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
        } else {
            // it's a console sending the command
            return empty;
        }
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

class Controls{

    protected static void adminControl(EvenMoreFish plugin, String[] args, CommandSender sender) {

        // will only proceed after this if at least args[1] exists
        if (args.length == 1) {
            sender.sendMessage(Help.formAdminHelp());
            return;
        }

        switch (args[1].toLowerCase()) {

            // bumps the command to another method, if it's a little too complicated it gets bumped to yet another method
            case "competition":
                competitionControl(args, sender);
                break;

            case "fish":
                if (args.length == 3) {
                    for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                        if (args[2].equalsIgnoreCase(r.getValue())) {
                            ComponentBuilder builder = new ComponentBuilder();

                            if (r.getDisplayName() != null) builder.append(FishUtils.translateHexColorCodes(r.getDisplayName()), ComponentBuilder.FormatRetention.NONE);
                            else builder.append(FishUtils.translateHexColorCodes(r.getColour() + "&l" + r.getValue() + ": "), ComponentBuilder.FormatRetention.NONE);

                            for (Fish fish : EvenMoreFish.fishCollection.get(r)) {
                                if (fish.getDisplayName() != null) builder.append(FishUtils.translateHexColorCodes(r.getColour() + "[" + fish.getDisplayName() + "] "));
                                else builder.append(FishUtils.translateHexColorCodes(r.getColour() + "[" + fish.getName() + "] "));

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
                    StringBuilder using = new StringBuilder();

                    Player player = null;
                    if (args.length > 4) {
                        for (int section = 3; section < args.length; section++) {
                            if (args[section].startsWith("-p:")) {
                                if ((player = Bukkit.getPlayer(args[section].substring(3))) == null) {
                                    sender.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getErrorPrefix() + args[section] + " is not a known player."));
                                    break;
                                }
                            } else {
                                using.append(args[section]);
                                if (section != args.length - 1 && !(args[section+1].startsWith("-p:"))) using.append(" ");
                            }
                        }
                    } else {
                        using = new StringBuilder(args[3]);
                    }

                    if (sender instanceof Player) {
                        for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                            if (args[2].equalsIgnoreCase(r.getValue())) {
                                for (Fish f : EvenMoreFish.fishCollection.get(r)) {
                                    if (f.getName().equalsIgnoreCase(using.toString())) {

                                        if (player == null) {
                                            f.setFisherman(((Player) sender).getUniqueId());
                                        } else {
                                            f.setFisherman((player).getUniqueId());
                                        }

                                        f.init();

                                        if (f.getFactory().getMaterial() != Material.AIR) {
                                            if (player == null) FishUtils.giveItems(Collections.singletonList(f.give()), (Player) sender);
                                            else FishUtils.giveItems(Collections.singletonList(f.give()), player);
                                        }

                                        if (player != null) {
                                            sender.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getAdminPrefix() + "You have given " + player.getName() + " a " + using));
                                        }

                                        break;

                                    }
                                }
                            }
                        }
                    } else {
                        EvenMoreFish.msgs.disabledInConsole();
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

            case "bait":
                if (args.length >= 3) {

                    // Some baits will probably have spaces in, this sorts out that issue.
                    StringBuilder builtName = new StringBuilder();
                    Player player = null;

                    for (int i = 2; i < args.length; i++) {
                        if (args[i].startsWith("-p:")) {
                            if ((player = Bukkit.getPlayer(args[i].substring(3))) == null) {
                                sender.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getErrorPrefix() + args[i] + " is not a known player."));
                                break;
                            }
                        } else {
                            builtName.append(args[i]);
                            if (i != args.length - 1 && !(args[i+1].startsWith("-p:"))) builtName.append(" ");
                        }
                    }

                    // Finding the bait and giving it to the (presumably) admin.
                    for (String baitID : EvenMoreFish.baits.keySet()) {
                        if (baitID.equalsIgnoreCase(builtName.toString())) {
                            Bait bait = EvenMoreFish.baits.get(baitID);
                            if (sender instanceof Player) {
                                if (player == null) FishUtils.giveItems(Collections.singletonList(bait.create()), (Player) sender);
                                else {
                                    FishUtils.giveItems(Collections.singletonList(bait.create()), player);
                                    sender.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getAdminPrefix() + "You have given " + player.getName() + " a " + baitID));
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Command cannot be run from console.");
                            }
                        }
                    }
                } else {
                    sender.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getErrorPrefix() + "Must provide a name of bait."));
                }

                break;

            case "reload":

                EvenMoreFish.fishFile.reload();
                EvenMoreFish.raritiesFile.reload();
                EvenMoreFish.baitFile.reload();

                plugin.reload();

                Message message = new Message().setMSG(EvenMoreFish.msgs.getReloaded());
                if (sender instanceof Player) message.setReceiver((Player) sender);
                sender.sendMessage(message.toString());
                break;

            case "version":
                Message msg = new Message().setMSG(
                        EvenMoreFish.msgs.getSTDPrefix() + "EvenMoreFish by Oheers " + plugin.getDescription().getVersion() + "\n" +
                                EvenMoreFish.msgs.getSTDPrefix() + "MCV: " + Bukkit.getServer().getVersion() + "\n" +
                                EvenMoreFish.msgs.getSTDPrefix() + "SSV: " + Bukkit.getServer().getBukkitVersion() + "\n" +
                                EvenMoreFish.msgs.getSTDPrefix() + "Online: " + Bukkit.getServer().getOnlineMode()
                );
                if (sender instanceof Player) msg.setReceiver((Player) sender);
                sender.sendMessage(msg.toString());
                break;
            default:
                sender.sendMessage(Help.formAdminHelp());
        }
    }

    protected static void competitionControl(String[] args, CommandSender player) {
        if (args.length == 2) {
            player.sendMessage(Help.formCompetitionHelp());
        } else {
            {
                if (args[2].equalsIgnoreCase("start")) {
                    // if the admin has only done /emf admin competition start
                    if (args.length < 4) {
                        startComp(Integer.toString(EvenMoreFish.mainConfig.getCompetitionDuration()*60), player, CompetitionType.LARGEST_FISH);
                    } else {
                        if (args.length < 5) {
                            startComp(args[3], player, CompetitionType.LARGEST_FISH);
                        } else {
                            try {
                                startComp(args[3], player, CompetitionType.valueOf(args[4].toUpperCase()));
                            } catch (IllegalArgumentException iae) {
                                player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getInvalidType()));
                            }
                        }
                    }
                }

                else if (args[2].equalsIgnoreCase("end")) {
                    if (Competition.isActive()) {
                        EvenMoreFish.active.end();
                    } else {
                        player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.competitionNotRunning()));
                    }
                } else {
                    player.sendMessage(Help.formCompetitionHelp());
                }
            }
        }
    }

    protected static void startComp(String argsDuration, CommandSender player, CompetitionType type) {
        if (Competition.isActive()) {
            player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.competitionRunning()));
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

                EvenMoreFish.active = comp;
                comp.begin(true);
            } else {
                player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.notInteger()));
            }
        } catch (NumberFormatException nfe) {
            player.sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.notInteger()));
        }
    }
}

class Help {

    public static String formGeneralHelp(CommandSender user) {

        StringBuilder out = new StringBuilder();
        List<String> commands = EvenMoreFish.msgs.getGeneralHelp();

        String escape = "\n";
        if (EvenMoreFish.permission != null && user != null) {
            for (int i=0; i<commands.size(); i++) {
                if (i == commands.size()-1) escape = "";
                if (commands.get(i).contains("/emf admin")) {
                    if (EvenMoreFish.permission.has(user, "emf.admin")) out.append(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getSTDPrefix() + commands.get(i) + escape));
                } else if (commands.get(i).contains("/emf top")) {
                    if (EvenMoreFish.permission.has(user, "emf.top")) out.append(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getSTDPrefix() + commands.get(i) + escape));
                } else if (commands.get(i).contains("/emf shop")) {
                    if (EvenMoreFish.permission.has(user, "emf.shop")) out.append(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getSTDPrefix() + commands.get(i) + escape));
                } else out.append(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getSTDPrefix() + commands.get(i) + escape));
            }
        } else {
            for (int i=0; i<commands.size(); i++) {
                if (i == commands.size()-1) escape = "";
                out.append(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getSTDPrefix() + commands.get(i) + escape));
            }
        }

        return out.toString();

    }

    public static String formCompetitionHelp() {

        StringBuilder out = new StringBuilder();
        List<String> commands = EvenMoreFish.msgs.getCompetitionHelp();

        String escape = "\n";
        for (int i=0; i<commands.size(); i++) {
            if (i == commands.size()-1) escape = "";
            out.append(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getSTDPrefix() + commands.get(i) + escape));
        }

        return out.toString();

    }

    public static String formAdminHelp() {

        StringBuilder out = new StringBuilder();
        List<String> commands = EvenMoreFish.msgs.getAdminHelp();

        String escape = "\n";
        for (int i=0; i<commands.size(); i++) {
            if (i == commands.size()-1) escape = "";
            out.append(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getSTDPrefix() + commands.get(i) + escape));
        }

        return out.toString();

    }

}
