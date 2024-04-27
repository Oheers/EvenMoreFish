package com.oheers.fish;

import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.permissions.AdminPerms;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URL;
import java.util.Scanner;

public class UpdateChecker {

    private final EvenMoreFish plugin;
    private final int resourceID;


    public UpdateChecker(final EvenMoreFish plugin, final int resourceID) {
        this.plugin = plugin;
        this.resourceID = resourceID;
    }

    public String getVersion() {
        try (final Scanner scanner = new Scanner(new URL("https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=" + resourceID).openStream())) {
            return ((JSONObject) new JSONParser().parse(scanner.nextLine())).get("current_version").toString();
        } catch (Exception ignored) {
            EvenMoreFish.getInstance().getLogger().warning("EvenMoreFish failed to check for updates against the spigot website, to check manually go to https://www.spigotmc.org/resources/evenmorefish.91310/updates");
            return plugin.getDescription().getVersion();
        }

    }
}

class UpdateNotify implements Listener {

    @EventHandler
    // informs admins with emf.admin permission that the plugin needs updating
    public void playerJoin(PlayerJoinEvent event) {
        if (!EvenMoreFish.getInstance().isUpdateAvailable()) {
            return;
        }

        if (EvenMoreFish.getInstance().getPermission().playerHas(event.getPlayer(), AdminPerms.UPDATE_NOTIFY)) {
            new Message(ConfigMessage.ADMIN_UPDATE_AVAILABLE).broadcast(event.getPlayer(), false);
        }

    }
}

