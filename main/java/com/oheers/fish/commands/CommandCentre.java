package com.oheers.fish.commands;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.commands.admin.Reload;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CommandCentre implements TabCompleter, CommandExecutor {

    private static final List<String> empty = Arrays.asList();

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

        switch (args[0].toLowerCase()) {
            case "admin":
                if (EvenMoreFish.permission.has(sender, "emf.admin")) {
                    // Checks for a value after /emf admin
                    if (args.length == 1) {
                        // sends admin information
                    } else {
                        switch (args[1].toLowerCase()) {
                            case "reload":
                                Reload.run();
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.RELOADED));
                        }
                    }
                } else {
                    Message message = new Message().setMSG(Messages.noPermission);
                    sender.sendMessage(message.toString());
                }
            default:
                sender.sendMessage("info about the emf plugin to go here.");
        }
    }

    private static List<String> emfTabs, adminTabs, TEMP_townTabCompletes;

    public static void loadTabCompletes() {
        adminTabs = Arrays.asList(
                "reload",
                "fakereload"
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


            switch (args[0].toLowerCase()) {

                case "admin": if (EvenMoreFish.permission.has(sender, "emf.admin")) {
                    return l(args, adminTabs);
                } else { return empty; }

                // there isn't an args[0], the player is only at /town
                default: if (args.length == 1) {
                    if (EvenMoreFish.permission.has(sender, "emf.admin")) {

                        // Checks if the player's halfway through writing "admin" and adds it to the temp list if so.
                        TEMP_townTabCompletes = l(args, emfTabs);
                        if ("admin".startsWith(args[args.length-1].toLowerCase())) {
                            TEMP_townTabCompletes.add("admin");
                        }
                        return TEMP_townTabCompletes;

                    }

                    return l(args, emfTabs);

                } else { return empty; }

            }
            // it is a console sending the command
        } else {

            return empty;
        }
    }

    // works out how far the player is into the tab and reduces the returned list accordingly
    private List<String> l(String[] progress, List<String> total) {
        List<String> prep = new ArrayList<>();
        for (String s : total) {
            if (s.startsWith(progress[progress.length-1].toLowerCase())) {
                prep.add(s);
            }
        }

        return prep;
    }
}
