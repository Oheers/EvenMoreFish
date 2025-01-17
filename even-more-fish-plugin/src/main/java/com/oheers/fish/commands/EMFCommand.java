package com.oheers.fish.commands;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.commands.arguments.ArgumentHelper;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.PrefixType;
import com.oheers.fish.gui.guis.ApplyBaitsGUI;
import com.oheers.fish.gui.guis.MainMenuGUI;
import com.oheers.fish.gui.guis.SellGUI;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import com.oheers.fish.selling.SellHelper;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EMFCommand {

    private final Map<String, String> commandUsages = new HashMap<>();

    private final CommandAPICommand command = new CommandAPICommand(MainConfig.getInstance().getMainCommandName())
            .withAliases(MainConfig.getInstance().getMainCommandAliases().toArray(String[]::new))
            .withSubcommands(
                    getNext(),
                    getToggle(),
                    getGui(),
                    getHelp(),
                    getTop(),
                    getShop(),
                    getSellAll(),
                    getApplyBaits(),
                    new AdminCommand("admin").getCommand()
            )
            .executes(info -> {
                sendHelpMessage(info.sender());
            });

    public CommandAPICommand getCommand() {
        return command;
    }

    private CommandAPICommand getNext() {
        commandUsages.putIfAbsent(
                "next",
                ConfigMessage.HELP_GENERAL_NEXT.getMessage().getLegacyMessage()
        );
        return new CommandAPICommand("next")
                .withPermission(UserPerms.NEXT)
                .executes(info -> {
                    AbstractMessage message = Competition.getNextCompetitionMessage();
                    message.prependMessage(PrefixType.DEFAULT.getPrefix());
                    message.send(info.sender());
                });
    }

    private CommandAPICommand getToggle() {
        commandUsages.putIfAbsent(
                "toggle",
                ConfigMessage.HELP_GENERAL_TOGGLE.getMessage().getLegacyMessage()
        );
        return new CommandAPICommand("toggle")
                .withPermission(UserPerms.TOGGLE)
                .executesPlayer(info -> {
                    EvenMoreFish.getInstance().performFishToggle(info.sender());
                });
    }

    private CommandAPICommand getGui() {
        commandUsages.putIfAbsent(
                "gui",
                ConfigMessage.HELP_GENERAL_GUI.getMessage().getLegacyMessage()
        );
        return new CommandAPICommand("gui")
                .withPermission(UserPerms.GUI)
                .executesPlayer(info -> {
                    new MainMenuGUI(info.sender()).open();
                });
    }

    private CommandAPICommand getHelp() {
        commandUsages.putIfAbsent(
                "help",
                ConfigMessage.HELP_GENERAL_HELP.getMessage().getLegacyMessage()
        );
        return new CommandAPICommand("help")
                .withPermission(UserPerms.HELP)
                .executes(info -> {
                    sendHelpMessage(info.sender());
                });
    }

    private CommandAPICommand getTop() {
        commandUsages.putIfAbsent(
                "top",
                ConfigMessage.HELP_GENERAL_TOP.getMessage().getLegacyMessage()
        );
        return new CommandAPICommand("top")
                .withPermission(UserPerms.TOP)
                .executesPlayer(info -> {
                    Competition active = Competition.getCurrentlyActive();
                    if (active == null) {
                        ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(info.sender());
                        return;
                    }
                    active.sendPlayerLeaderboard(info.sender());
                })
                .executes(info -> {
                    Competition active = Competition.getCurrentlyActive();
                    if (active == null) {
                        ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(info.sender());
                        return;
                    }
                    active.sendConsoleLeaderboard(info.sender());
                });
    }

    private CommandAPICommand getShop() {
        commandUsages.putIfAbsent(
                "shop",
                ConfigMessage.HELP_GENERAL_SHOP.getMessage().getLegacyMessage()
        );
        return new CommandAPICommand("shop")
                .withPermission(UserPerms.SHOP)
                .withArguments(
                        ArgumentHelper.getPlayerArgument("target").setOptional(true)
                )
                .executes((sender, args) -> {
                    Player player = (Player) args.get("target");
                    if (player == null){
                        if (!(sender instanceof Player p)) {
                            ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
                            return;
                        }
                        player = p;
                    }
                    if (!checkEconomy(player)) {
                        return;
                    }
                    if (sender == player) {
                        new SellGUI(player, SellGUI.SellState.NORMAL, null).open();
                        return;
                    }
                    if (!sender.hasPermission(AdminPerms.ADMIN)) {
                        ConfigMessage.NO_PERMISSION.getMessage().send(sender);
                        return;
                    }
                    new SellGUI(player, SellGUI.SellState.NORMAL, null).open();
                    AbstractMessage message = ConfigMessage.ADMIN_OPEN_FISH_SHOP.getMessage();
                    message.setPlayer(player);
                    message.send(sender);
                });
    }

    private CommandAPICommand getSellAll() {
        commandUsages.putIfAbsent(
                "sellall",
                ConfigMessage.HELP_GENERAL_SELLALL.getMessage().getLegacyMessage()
        );
        return new CommandAPICommand("sellall")
                .withPermission(UserPerms.SELL_ALL)
                .executesPlayer(info -> {
                    Player player = info.sender();
                    if (checkEconomy(player)) {
                        new SellHelper(player.getInventory(), player).sellFish();
                    }
                });
    }

    private CommandAPICommand getApplyBaits() {
        commandUsages.putIfAbsent(
                "applybaits",
                ConfigMessage.HELP_GENERAL_APPLYBAITS.getMessage().getLegacyMessage()
        );
        return new CommandAPICommand("applybaits")
                .withPermission(UserPerms.APPLYBAITS)
                .executesPlayer(info -> {
                    new ApplyBaitsGUI(info.sender(), null).open();
                });
    }

    private void sendHelpMessage(@NotNull CommandSender sender) {
        HelpMessageBuilder.create(commandUsages).sendMessage(sender);
    }

    private boolean checkEconomy(@NotNull CommandSender sender) {
        if (!Economy.getInstance().isEnabled()) {
            ConfigMessage.ECONOMY_DISABLED.getMessage().send(sender);
            return false;
        }
        return true;
    }

}
