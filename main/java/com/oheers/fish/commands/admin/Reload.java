package com.oheers.fish.commands.admin;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.Bukkit;

public class Reload {

    public static void run() {
        System.out.println("running...2");
        EvenMoreFish.fishFile.reload();
        EvenMoreFish.raritiesFile.reload();
        Bukkit.getPluginManager().getPlugin("EvenMoreFish").reloadConfig();
    }
}
