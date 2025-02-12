package com.oheers.fish.gui.guis.journal;

import com.oheers.fish.FishUtils;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CodexCommandExecutor implements CommandExecutor, TabCompleter {
    private final DatabaseManager dbManager;
    //private final FileConfiguration config;

    public CodexCommandExecutor(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("emfcodex.use")) {
            //sender.sendMessage(FishUtils.translateColorCodes(config.getString("messages.no_permission", "&cYou do not have permission to use this command.")));
            return false;
        }

        try {
            if (args.length == 1 || args.length == 2) {
                String rarity = args[0];
                Player targetPlayer;

                if (args.length == 2) {
                    targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer == null) {
                        //String playerNotFoundMessage = FishUtils.translateColorCodes(config.getString("messages.player_not_found", "&cPlayer {player} not found.").replace("{player}", args[1]));
                        //sender.sendMessage(playerNotFoundMessage);
                        return false;
                    }
                } else {
                    if (!(sender instanceof Player)) {
                        //String playerOnlyMessage = FishUtils.translateColorCodes(config.getString("messages.player_only", "&cThis command can only be used by players."));
                        //sender.sendMessage(playerOnlyMessage);
                        return false;
                    }
                    targetPlayer = (Player) sender;
                }

                Rarity rarityObj = FishManager.getInstance().getRarity(rarity);
                if (rarityObj == null) {
                    return false;
                }

                new FishJournalGui(targetPlayer, rarityObj).open();
                return true;
            } else {
                //String usageMessage = FishUtils.translateColorCodes(config.getString("messages.usage", "&cUsage: /{label} <rarity> [player]").replace("{label}", label));
                //sender.sendMessage(usageMessage);
                return false;
            }
        } catch (Exception e) {
            //plugin.getLogger().log(Level.SEVERE, "An error occurred while executing the command", e);
            sender.sendMessage(FishUtils.translateColorCodes("&cAn error occurred while executing the command. Please check the server logs for more details."));
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(FishManager.getInstance().getRarityMap().keySet());
        }
        return null;
    }
}