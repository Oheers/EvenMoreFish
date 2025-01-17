package com.oheers.fish.commands.acf;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.addons.AddonManager;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.addons.Addon;
import com.oheers.fish.api.reward.RewardManager;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitManager;
import com.oheers.fish.baits.BaitNBTManager;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.permissions.AdminPerms;
import de.tr7zw.changeme.nbtapi.NBT;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@CommandAlias("%main")
@Subcommand("admin")
@CommandPermission(AdminPerms.ADMIN)
public class AdminCommand extends BaseCommand {

    @Subcommand("rawItem")
    @Description("Outputs this item's raw NBT form for use in YAML")
    public void onRawItem(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
            return;
        }
        ItemStack handItem = player.getInventory().getItemInMainHand();
        String handItemNbt = NBT.itemStackToNBT(handItem).toString();

        // Ensure the handItemNbt is escaped for use in YAML
        // This could be slightly inefficient, but it is the only way I can currently think of.
        YamlDocument document = new ConfigBase().getConfig();
        document.set("rawItem", handItemNbt);
        handItemNbt = document.dump().replaceFirst("rawItem: ", "");

        TextComponent component = new TextComponent(handItemNbt);
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("Click to copy to clipboard."))
        ));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, handItemNbt));
        player.spigot().sendMessage(component);
    }

}
