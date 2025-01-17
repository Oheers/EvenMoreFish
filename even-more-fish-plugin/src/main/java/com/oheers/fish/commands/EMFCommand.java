package com.oheers.fish.commands;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.economy.Economy;
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
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EMFCommand {

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
                    new AdminCommand(this).getCommand()
            )
            .executes(info -> {
                sendHelpMessage(info.sender());
            });

    public CommandAPICommand getCommand() {
        return command;
    }

    private CommandAPICommand getNext() {
        return new CommandAPICommand("next")
                .withPermission(UserPerms.NEXT)
                .withFullDescription(ConfigMessage.HELP_GENERAL_NEXT.getMessage().getPlainTextMessage())
                .executes(info -> {
                    AbstractMessage message = Competition.getNextCompetitionMessage();
                    message.prependMessage(PrefixType.DEFAULT.getPrefix());
                    message.send(info.sender());
                });
    }

    private CommandAPICommand getToggle() {
        return new CommandAPICommand("toggle")
                .withPermission(UserPerms.TOGGLE)
                .withFullDescription(ConfigMessage.HELP_GENERAL_TOGGLE.getMessage().getPlainTextMessage())
                .executesPlayer(info -> {
                    EvenMoreFish.getInstance().performFishToggle(info.sender());
                });
    }

    private CommandAPICommand getGui() {
        return new CommandAPICommand("gui")
                .withPermission(UserPerms.GUI)
                .withFullDescription(ConfigMessage.HELP_GENERAL_GUI.getMessage().getPlainTextMessage())
                .executesPlayer(info -> {
                    new MainMenuGUI(info.sender()).open();
                });
    }

    private CommandAPICommand getHelp() {
        return new CommandAPICommand("help")
                .withPermission(UserPerms.HELP)
                .withFullDescription(ConfigMessage.HELP_GENERAL_HELP.getMessage().getPlainTextMessage())
                .executes(info -> {
                    sendHelpMessage(info.sender());
                });
    }

    private CommandAPICommand getTop() {
        return new CommandAPICommand("top")
                .withPermission(UserPerms.TOP)
                .withFullDescription(ConfigMessage.HELP_GENERAL_TOP.getMessage().getPlainTextMessage())
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
        return new CommandAPICommand("shop")
                .withPermission(UserPerms.SHOP)
                .withFullDescription(ConfigMessage.HELP_GENERAL_SHOP.getMessage().getPlainTextMessage())
                .withArguments(
                        new EntitySelectorArgument.OnePlayer("target").setOptional(true)
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
        return new CommandAPICommand("sellall")
                .withPermission(UserPerms.SELL_ALL)
                .withFullDescription(ConfigMessage.HELP_GENERAL_SELLALL.getMessage().getPlainTextMessage())
                .executesPlayer(info -> {
                    Player player = info.sender();
                    if (checkEconomy(player)) {
                        new SellHelper(player.getInventory(), player).sellFish();
                    }
                });
    }

    private CommandAPICommand getApplyBaits() {
        return new CommandAPICommand("applybaits")
                .withPermission(UserPerms.APPLYBAITS)
                .withFullDescription(ConfigMessage.HELP_GENERAL_APPLYBAITS.getMessage().getPlainTextMessage())
                .executesPlayer(info -> {
                    new ApplyBaitsGUI(info.sender(), null).open();
                });
    }

    // TODO we need a custom HelpMessageBuilder for this.
    public void sendHelpMessage(@NotNull CommandSender sender) {
        ConfigMessage.HELP_GENERAL_TITLE.getMessage().send(sender);
        /*
        help.getHelpEntries().forEach(helpEntry -> {
            AbstractMessage helpMessage = ConfigMessage.HELP_FORMAT.getMessage();
            helpMessage.setVariable("{command}", "/" + helpEntry.getCommand());
            helpMessage.setVariable("{description}", helpEntry.getDescription());
            helpMessage.send(sender);
        });
         */
    }

    private boolean checkEconomy(@NotNull CommandSender sender) {
        if (!Economy.getInstance().isEnabled()) {
            ConfigMessage.ECONOMY_DISABLED.getMessage().send(sender);
            return false;
        }
        return true;
    }

}
