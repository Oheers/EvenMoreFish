package com.oheers.fish.commands.acf;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.messages.ConfigMessage;
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

    @Subcommand("applybaits")
    @CommandPermission(UserPerms.APPLYBAITS)
    @Description("%desc_general_applybaits%")
    public void onApplyBaits(final Player sender) {
        new ApplyBaitsGUI(sender, null).open();
    }

}
