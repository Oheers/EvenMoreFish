package com.oheers.fish.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.PrefixType;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import com.oheers.fish.selling.SellGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


@CommandAlias(CommandUtil.MAIN_COMMAND)
public class EMFCommand extends BaseCommand {

    @Subcommand("next")
    public void onNext(final CommandSender sender) {
        Message message = Competition.getNextCompetitionMessage();
        message.usePrefix(PrefixType.DEFAULT);
        message.broadcast(sender, true, true);
    }

    @Subcommand("toggle")
    @CommandPermission(UserPerms.TOGGLE)
    public void onToggle(final Player player) {
        if (EvenMoreFish.getInstance().getDisabledPlayers().contains(player.getUniqueId())) {
            EvenMoreFish.getInstance().getDisabledPlayers().remove(player.getUniqueId());
            new Message(ConfigMessage.TOGGLE_ON).broadcast(player, true, false);
            return;
        }

        EvenMoreFish.getInstance().getDisabledPlayers().add(player.getUniqueId());
        new Message(ConfigMessage.TOGGLE_OFF).broadcast(player, true, false);
    }

    @Subcommand("migrate")
    @CommandPermission(AdminPerms.MIGRATE)
    public void onMigrate(final CommandSender sender) {
        EvenMoreFish.getScheduler().runTaskAsynchronously(() -> EvenMoreFish.getInstance().getDatabaseV3().migrateLegacy(sender));
    }

    @Default
    @Subcommand("help")
    public void onHelp(final CommandSender sender) {
        sender.sendMessage(CommandUtil.formGeneralHelp(sender));
    }

    @Subcommand("top")
    @CommandPermission(UserPerms.TOP)
    public void onTop(final CommandSender sender) {
        if (!Competition.isActive()) {
            new Message(ConfigMessage.NO_COMPETITION_RUNNING).broadcast(sender, true, true);
            return;
        }

        if (sender instanceof Player) {
            EvenMoreFish.getInstance().getActiveCompetition().sendPlayerLeaderboard((Player) sender);
            return;
        }

        if (sender instanceof ConsoleCommandSender) {
            EvenMoreFish.getInstance().getActiveCompetition().sendConsoleLeaderboard((ConsoleCommandSender) sender);
        }
    }

    @Subcommand("shop")
    @CommandPermission(UserPerms.SHOP)
    public void onShop(final CommandSender sender, @Optional final OnlinePlayer onlinePlayer) {
        if (MainConfig.getInstance().isEconomyDisabled()) {
            new Message(ConfigMessage.ECONOMY_DISABLED).broadcast(sender, true, false);
            return;
        }


        if (EvenMoreFish.getInstance().getPermission().has(sender, AdminPerms.ADMIN)) {
            new SellGUI(onlinePlayer.player, true);
            Message message = new Message(ConfigMessage.ADMIN_OPEN_FISH_SHOP);
            message.setPlayer(onlinePlayer.player.getName());
            message.broadcast(sender, true, true);
            return;
        }

        if (sender instanceof Player) {
            new SellGUI((Player) sender, true);
        }
    }

    @Subcommand("sellall")
    @CommandPermission(UserPerms.SELL_ALL)
    public void onSellAll(final Player sender) {
        if (MainConfig.getInstance().isEconomyDisabled()) {
            new Message(ConfigMessage.ECONOMY_DISABLED).broadcast(sender, true, false);
            return;
        }


        SellGUI gui = new SellGUI(sender, false);
        gui.sell(true);
    }


}