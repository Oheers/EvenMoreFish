package com.oheers.fish;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Consumer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;

public class UpdateChecker {

    private final EvenMoreFish plugin;
    private final int resourceID;


    public UpdateChecker(final EvenMoreFish plugin, final int resourceID) {
        this.plugin = plugin;
        this.resourceID = resourceID;
    }

    public String getVersion() {
        String version;
        try {
            version = ((JSONObject) new JSONParser().parse(new Scanner(new URL("https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=" + resourceID).openStream()).nextLine())).get("current_version").toString();
        } catch (Exception ignored) {
            version = plugin.getDescription().getVersion();
            Bukkit.getLogger().log(Level.WARNING, "EvenMoreFish failed to check for updates against the spigot website, to check manually go to https://www.spigotmc.org/resources/evenmorefish.91310/updates");
        }

        return version;
    }
}

class UpdateNotify implements Listener {

    @EventHandler
    // informs admins with emf.admin permission that the plugin needs updating
    public void playerJoin(PlayerJoinEvent event) {
        if (EvenMoreFish.isUpdateAvailable) {
            if (EvenMoreFish.permission.playerHas(event.getPlayer(), "emf.admin")) {
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', EvenMoreFish.msgs.getAdminPrefix() + "There is an update available: " + "https://www.spigotmc.org/resources/evenmorefish.91310/updates"));
            }
        }
    }
}
