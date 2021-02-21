package com.oheers.fish.commands;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.commands.admin.Reload;
import com.oheers.fish.config.messages.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCentre implements CommandExecutor {

    private static final String NOPERMS = "&cYou do not have permission to run that command.";
    private static final String RELOADED = "&a[EvenMoreFish] &rsuccessfully reloaded.";

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
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', RELOADED));
                        }
                    }
                } else {
                    sender.sendMessage(Messages.renderMessage(Messages.noPermission, null, null, null, null, null));
                }
        }
    }
}
