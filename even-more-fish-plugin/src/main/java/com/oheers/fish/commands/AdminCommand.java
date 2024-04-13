package com.oheers.fish.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.addons.AddonManager;
import com.oheers.fish.api.addons.Addon;
import com.oheers.fish.api.reward.RewardManager;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.config.BaitFile;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.fishing.items.Rarity;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandAlias(CommandUtil.ADMIN_COMMAND)
public class AdminCommand extends BaseCommand {
    @Subcommand("fish")
    public static class FishSubCommand extends BaseCommand {

    }

    @Subcommand("comp")
    public static class CompetitionSubCommand extends BaseCommand {

        @Subcommand("start")
        public void onStart(final CommandSender sender, @Default("%duration") @Conditions("limits:min=1") Integer duration, @Default("LARGEST_FISH") @Optional CompetitionType type) {
            if (Competition.isActive()) {
                new Message(ConfigMessage.COMPETITION_ALREADY_RUNNING).broadcast(sender, true, false);
                return;
            }


            Competition comp = new Competition(duration, type, new ArrayList<>());

            comp.setCompetitionName("[admin_started]");
            comp.setAdminStarted(true);
            comp.initRewards(null, true);
            comp.initBar(null);
            comp.initGetNumbersNeeded(null);
            comp.initStartSound(null);

            EvenMoreFish.getInstance().setActiveCompetition(comp);
            comp.begin(true);
        }

        @Subcommand("end")
        public void onEnd(final CommandSender sender) {
            if (Competition.isActive()) {
                EvenMoreFish.getInstance().getActiveCompetition().end();
                return;
            }

            new Message(ConfigMessage.NO_COMPETITION_RUNNING).broadcast(sender, true, true);
        }

        @Default
        @Subcommand("help")
        public void onHelp(final CommandSender sender) {
            new Message(ConfigMessage.HELP_COMPETITION).broadcast(sender, true, false);
        }
    }

    @Subcommand("nbt-rod")
    public void onNbtRod() {
    }


    @Subcommand("bait")
    public void onBait() {

    }

    @Subcommand("clearbaits")
    public void onClearBaits() {


    }


    @Subcommand("reload")
    public void onReload(final CommandSender sender) {
        FishFile.getInstance().reload();
        RaritiesFile.getInstance().reload();
        BaitFile.getInstance().reload();

        EvenMoreFish.getInstance().reload();

        new Message(ConfigMessage.RELOAD_SUCCESS).broadcast(sender, true, false);
    }


    public void onAddons(final CommandSender sender) {
        final AddonManager addonManager = EvenMoreFish.getInstance().getAddonManager();
        final String messageFormat = "Addon: %s, Loading: %b";
        final List<String> messageList = new ArrayList<>();
        for (final Map.Entry<String, Addon> entry : addonManager.getAddonMap().entrySet()) {
            final String prefix = entry.getKey();
            messageList.add(String.format(messageFormat, prefix, addonManager.isLoading(prefix)));
        }

        new Message(messageList).broadcast(sender, true, false);
    }

    @Subcommand("version")
    public void onVersion(final CommandSender sender) {
        int fishCount = 0;
        for (Rarity r : EvenMoreFish.getInstance().getFishCollection().keySet()) {
            fishCount += EvenMoreFish.getInstance().getFishCollection().get(r).size();
        }

        String msgString = Messages.getInstance().getSTDPrefix() + "EvenMoreFish by Oheers " + EvenMoreFish.getInstance().getDescription().getVersion() + "\n" +
                Messages.getInstance().getSTDPrefix() + "MCV: " + Bukkit.getServer().getVersion() + "\n" +
                Messages.getInstance().getSTDPrefix() + "SSV: " + Bukkit.getServer().getBukkitVersion() + "\n" +
                Messages.getInstance().getSTDPrefix() + "Online: " + Bukkit.getServer().getOnlineMode() + "\n" +
                Messages.getInstance().getSTDPrefix() + "Loaded: Rarities(" + EvenMoreFish.getInstance().getFishCollection().size() + ") Fish(" +
                fishCount + ") Baits(" + EvenMoreFish.getInstance().getBaits().size() + ") Competitions(" + EvenMoreFish.getInstance().getCompetitionQueue().getSize() + ")\n" +
                Messages.getInstance().getSTDPrefix();

        msgString += "Database Engine: " + getDatabaseVersion();

        Message msg = new Message(msgString);
        msg.broadcast(sender, true, false);
    }

    private String getDatabaseVersion() {
        if (!MainConfig.getInstance().databaseEnabled()) {
            return "None";
        }

        if (EvenMoreFish.getInstance().getDatabaseV3().usingVersionV2()) {
            return "V2";
        }

        return "V3";
    }

    @Subcommand("rewardtypes")
    public void onRewardTypes(final CommandSender sender) {
        TextComponent message = new TextComponent(new Message(ConfigMessage.ADMIN_LIST_REWARD_TYPES).getRawMessage(true, false));
        ComponentBuilder builder = new ComponentBuilder(message);

        RewardManager.getInstance().getRegisteredRewardTypes().forEach(rewardType -> {
            TextComponent component = new TextComponent(rewardType.getIdentifier());
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(
                            "Author: " + rewardType.getAuthor() + "\n" +
                                    "Registered Plugin: " + rewardType.getPlugin().getName()
                    )
            ));
            builder.append(component).append(", ");
        });
        sender.spigot().sendMessage(builder.create());
    }

    @Default
    @Subcommand("help")
    public void onHelp(final CommandSender sender) {
        new Message(ConfigMessage.HELP_ADMIN).broadcast(sender, true, false);
    }
}
