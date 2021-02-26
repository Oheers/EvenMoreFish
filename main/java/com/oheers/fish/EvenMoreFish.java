package com.oheers.fish;

import com.oheers.fish.commands.CommandCentre;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.config.messages.MessageFile;
import com.oheers.fish.database.Database;
import com.oheers.fish.fishing.FishEvent;
import com.oheers.fish.fishing.items.Names;
import com.oheers.fish.fishing.items.Rarity;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.stream.events.DTD;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class EvenMoreFish extends JavaPlugin {

    public static FishFile fishFile;
    public static RaritiesFile raritiesFile;
    public static MessageFile messageFile;

    public static Permission permission = null;

    public static Map<Rarity, Set<String>> fish = new HashMap<>();

    public void onEnable() {


        listeners();
        commands();

        // could not setup permissions.
        if (!setupPermissions()) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, "EvenMoreFish couldn't hook into Vault. Disabling to prevent serious problems.");
            this.setEnabled(false);
        }

        fishFile = new FishFile(this);
        raritiesFile = new RaritiesFile(this);
        messageFile = new MessageFile(this);

        Names names = new Names();
        names.loadRarities();

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        if (MainConfig.database) {

            // Attempts to connect to the database if enabled
            try {
                if (!Database.dbExists()) {
                    Database.createDatabase();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        Bukkit.getServer().getLogger().log(Level.INFO, "EvenMoreFish by Oheers : Enabled");

    }

    public void onDisable() {

        Bukkit.getServer().getLogger().log(Level.INFO, "EvenMoreFish by Oheers : Disabled");

    }

    private void listeners() {

        getServer().getPluginManager().registerEvents(new FishEvent(), this);

    }

    private void commands() {
        getCommand("evenmorefish").setExecutor(new CommandCentre());
        CommandCentre.loadTabCompletes();
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        return permission != null;
    }

}
