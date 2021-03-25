package com.oheers.fish;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class CommandCentre implements TabCompleter, CommandExecutor {

    private static final List<String> empty = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Aliases are set in the plugin.yml
        if (cmd.getName().equalsIgnoreCase("evenmorefish")) {
            if (args.length == 0) {
                sender.sendMessage(Help.std_help);
            } else {
                control((Player) sender, args);
            }
        }

        return true;
    }

    private void control(Player sender, String[] args) {

        // we've already checked that that args exist
        switch (args[0].toLowerCase()) {
            case "admin":
                if (EvenMoreFish.permission.has(sender, "emf.admin")) {
                    Controls.adminControl(args, sender);
                } else {
                    sender.sendMessage(new Message().setMSG(Messages.noPermission).toString());
                }
                break;
            case "top":
                if (EvenMoreFish.active == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.competitionNotRunning));
                } else {
                    sender.sendMessage(EvenMoreFish.active.getLeaderboard(false));
                }
                break;
            default:
                sender.sendMessage(Help.std_help);
        }
    }

    private static List<String> emfTabs, adminTabs, compTabs;

    public static void loadTabCompletes() {
        adminTabs = Arrays.asList(
                "reload",
                "competition"
        );

        compTabs = Arrays.asList(
                "start",
                "end"
        );

        emfTabs = Arrays.asList(
                "top",
                "command1",
                "command2"
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {

            switch (args.length) {
                case 1:
                    if (EvenMoreFish.permission.has(sender, "emf.admin")) {

                        // creates a temp version of tablist where only the qualified completes go through
                        List<String> TEMP_townTabCompletes = l(args, emfTabs);
                        // if the player is writing "admin" it adds it to the temporary tabcomplete list
                        if ("admin".startsWith(args[args.length - 1].toLowerCase())) {
                            TEMP_townTabCompletes.add("admin");
                        }
                        return TEMP_townTabCompletes;
                    }
                case 2:
                    // checks player has admin perms and has actually used "/emf admin" prior to the 2nd arg
                    if (args[0].equalsIgnoreCase("admin") && EvenMoreFish.permission.has(sender, "emf.admin")) {
                        return l(args, adminTabs);
                    }
                case 3:
                    if (args[1].equalsIgnoreCase("competition") && args[0].equalsIgnoreCase("admin") && EvenMoreFish.permission.has(sender, "emf.admin")) {
                        return l(args, compTabs);
                    }
                default:
                    return empty;
            }
        } else {
            // it's a console sending the command
            return empty;
        }
    }

    // works out how far the player is into the tab and reduces the returned list accordingly
    private List<String> l(String[] progress, List<String> total) {
        List<String> prep = new ArrayList<>();
        for (String s : total) {
            if (s.startsWith(progress[progress.length - 1].toLowerCase())) {
                prep.add(s);
            }
        }

        return prep;
    }
}

class Controls {

    protected static void adminControl(String[] args, Player sender) {

        // will only proceed after this if at least args[1] exists
        if (args.length == 1) {
            sender.sendMessage(Help.admin_help);
            return;
        }

        switch (args[1].toLowerCase()) {

            // bumps the command to another method, if it's a little too complicated it gets bumped to yet another method
            case "competition":
                competitionControl(args, sender);
                break;

            case "reload":
                EvenMoreFish.fishFile.reload();
                EvenMoreFish.raritiesFile.reload();
                Bukkit.getPluginManager().getPlugin("EvenMoreFish").reloadConfig();
                sender.sendMessage(new Message().setMSG(Messages.RELOADED).toString());
                break;

            default:
                sender.sendMessage(Help.admin_help);
        }
    }

    protected static void competitionControl(String[] args, Player player) {
        if (args.length == 2) {
            player.sendMessage(Help.comp_help);
        } else {
            {
                if (args[2].equalsIgnoreCase("start")) {
                    startComp(args[3], player);
                }

                else if (args[2].equalsIgnoreCase("end")) {
                    if (EvenMoreFish.active != null) {
                        EvenMoreFish.active.end();
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.competitionNotRunning));
                    }
                }
            }
        }
    }

    protected static void startComp(String argsDuration, Player player) {

        if (EvenMoreFish.active != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.competitionRunning));
            return;
        }

        try {
            // converts argsDuration to an integer (throwing exceptions) and starts a competition with that
            int duration = Integer.parseInt(argsDuration);
            // I've just discovered /emf admin competition start -1 causes some funky stuff - so this prevents that.
            if (duration > 0) {
                Competition comp = new Competition(duration);
                comp.start();
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.notInt));
            }
        } catch (NumberFormatException nfe) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.notInt));
        }
    }
}

class Help {

    public static Map<String, String> cmdDictionary = new HashMap<>();
    public static Map<String, String> adminDictionary = new HashMap<>();
    public static Map<String, String> compDictionary = new HashMap<>();

    public static String std_help, admin_help, comp_help;

    // puts values into the command dictionaries for later use in /emf help and what not
    public static void loadValues() {

        cmdDictionary.put("help", "Shows you this page");
        cmdDictionary.put("admin", "Admin command help page");

        adminDictionary.put("emf admin competition <start/end> <time(seconds)>", "Starts or stops a competition");
        adminDictionary.put("emf admin reload", "Reloads the plugin's config files");

        compDictionary.put("emf admin competition start <time<seconds>", "Starts a competition of a specified duration");
        compDictionary.put("emf admin competition end <time<seconds>", "Ends the current competition (if there is one)");

        std_help = formString(cmdDictionary);
        admin_help = formString(adminDictionary);
        comp_help = formString(compDictionary);

        // gc
        cmdDictionary = null;
        adminDictionary = null;
        compDictionary = null;

    }

    public static String formString(Map<String, String> dictionary) {

        StringBuilder out = new StringBuilder();

        out.append(ChatColor.translateAlternateColorCodes('&', Messages.prefix_std + "----- &a&lEvenMoreFish &r-----\n"));

        for (String s : dictionary.keySet()) {
            out.append(new Message().setCMD(s).setDesc(cmdDictionary.get(s)).setMSG(Messages.emf_help).toString()).append("\n");
        }

        return out.toString();

    }

}
