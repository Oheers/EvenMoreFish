package com.oheers.fish;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandCentre implements TabCompleter, CommandExecutor {

    private static final List<String> empty = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Aliases are set in the plugin.yml
        if (cmd.getName().equalsIgnoreCase("evenmorefish")) {
            if (args.length == 0) {
                // send plugin info
            } else {
                control((Player) sender, args);
            }
        }

        return true;
    }

    private void control(Player sender, String[] args) {

        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {

                case "admin":
                    if (EvenMoreFish.permission.has(sender, "emf.admin")) {
                        Controls.adminControl(args, sender);
                    } break;

                default:
                    sender.sendMessage("help");
            }
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
                "test",
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
            sender.sendMessage("admin command list");
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
                sender.sendMessage("admin command list");
        }
    }

    protected static void competitionControl(String[] args, Player player) {
        if (args.length == 3) {
            player.sendMessage("competition commands");
        } else {
            {
                if (args[2].equalsIgnoreCase("start")) {
                    startComp(args[3], player);
                }
            }
        }
    }

    protected static void startComp(String argsDuration, Player player) {

        if (EvenMoreFish.active != null) {
            player.sendMessage("competition in progress");
            return;
        }

        try {
            // converts argsDuration to an integer (throwing exceptions) and starts a competition with that
            int duration = Integer.parseInt(argsDuration);
            Competition comp = new Competition(duration);
            comp.start();
        } catch (NumberFormatException nfe) {
            player.sendMessage("Not a number");
        }
    }
}
