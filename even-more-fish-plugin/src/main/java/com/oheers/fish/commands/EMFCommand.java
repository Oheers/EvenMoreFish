package com.oheers.fish.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.config.messages.PrefixType;
import com.oheers.fish.gui.guis.ApplyBaitsGUI;
import com.oheers.fish.gui.guis.MainMenuGUI;
import com.oheers.fish.gui.guis.SellGUI;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import com.oheers.fish.selling.SellHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


@CommandAlias("%main")
public class EMFCommand extends BaseCommand {

    @Subcommand("next")
    @Description("%desc_general_next")
    @CommandPermission(UserPerms.NEXT)
    public void onNext(final CommandSender sender) {
        AbstractMessage message = Competition.getNextCompetitionMessage();
        message.prependMessage(PrefixType.DEFAULT.getPrefix());
        message.send(sender);
    }

    @Subcommand("toggle")
    @Description("%desc_general_toggle")
    @CommandPermission(UserPerms.TOGGLE)
    public void onToggle(final Player player) {
        EvenMoreFish.getInstance().performFishToggle(player);
    }

    @Subcommand("gui")
    @Description("%desc_general_gui")
    @CommandPermission(UserPerms.GUI)
    public void onGui(final Player player) {
        new MainMenuGUI(player).open();
    }

    @Default
    @HelpCommand
    @CommandPermission(UserPerms.HELP)
    @Description("%desc_general_help")
    public void onHelp(final CommandHelp help, final CommandSender sender) {
        ConfigMessage.HELP_GENERAL_TITLE.getMessage().send(sender);
        help.getHelpEntries().forEach(helpEntry -> {
            AbstractMessage helpMessage = ConfigMessage.HELP_FORMAT.getMessage();
            helpMessage.setVariable("{command}", "/" + helpEntry.getCommand());
            helpMessage.setVariable("{description}", helpEntry.getDescription());
            helpMessage.send(sender);
        });
    }

    @Subcommand("top")
    @CommandPermission(UserPerms.TOP)
    @Description("%desc_general_top")
    public void onTop(final CommandSender sender) {
        if (!Competition.isActive()) {
            ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(sender);
            return;
        }

        if (sender instanceof Player player) {
            EvenMoreFish.getInstance().getActiveCompetition().sendPlayerLeaderboard(player);
            return;
        }

        if (sender instanceof ConsoleCommandSender consoleCommandSender) {
            EvenMoreFish.getInstance().getActiveCompetition().sendConsoleLeaderboard(consoleCommandSender);
        }
    }

    @Subcommand("shop")
    @CommandPermission(UserPerms.SHOP)
    @Description("%desc_general_shop")
    public void onShop(final CommandSender sender, @Optional final OnlinePlayer onlinePlayer) {
        if (!Economy.getInstance().isEnabled()) {
            ConfigMessage.ECONOMY_DISABLED.getMessage().send(sender);
            return;
        }

        if (onlinePlayer == null) {
            if (!(sender instanceof Player player)) {
                EvenMoreFish.getAdapter().createMessage("&cYou must specify a player when running from console.").send(sender);
                return;
            }
            new SellGUI(player, SellGUI.SellState.NORMAL, null).open();
            return;
        }

        if (sender.hasPermission(AdminPerms.ADMIN)) {
            new SellGUI(onlinePlayer.player, SellGUI.SellState.NORMAL, null).open();
            AbstractMessage message = ConfigMessage.ADMIN_OPEN_FISH_SHOP.getMessage();
            message.setPlayer(onlinePlayer.player);
            message.send(sender);
        }
    }

    @Subcommand("sellall")
    @CommandPermission(UserPerms.SELL_ALL)
    @Description("%desc_general_sellall")
    public void onSellAll(final Player sender) {
        if (!Economy.getInstance().isEnabled()) {
            ConfigMessage.ECONOMY_DISABLED.getMessage().send(sender);
            return;
        }
        new SellHelper(sender.getInventory(), sender).sellFish();
    }

    @Subcommand("applybaits")
    @CommandPermission(UserPerms.APPLYBAITS)
    @Description("%desc_general_applybaits%")
    public void onApplyBaits(final Player sender) {
        new ApplyBaitsGUI(sender, null).open();
    }

}
