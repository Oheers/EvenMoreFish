package com.oheers.fish.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.PrefixType;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import org.bukkit.command.CommandSender;
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
    public void onMigrate() {

    }

    @Default
    public void onHelp() {

    }
}
