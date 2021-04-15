package com.oheers.fish;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Consumer;

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

    public void getVersion(Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceID).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.WARNING, "EvenMoreFish failed to check for updates." + e.getMessage());
            }
        });
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
