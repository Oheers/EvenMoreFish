package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.gui.Button;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIConfig {

    private final EvenMoreFish plugin;
    private FileConfiguration config;

    public GUIConfig (EvenMoreFish plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File competitionsFile = new File(this.plugin.getDataFolder(), "guis.yml");

        if (!competitionsFile.exists()) {
            competitionsFile.getParentFile().mkdirs();
            this.plugin.saveResource("guis.yml", false);
        }

        this.config = new YamlConfiguration();

        try {
            this.config.load(competitionsFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getToggle(boolean toggleState) {
        if (toggleState) return this.config.getString("enabled-msg", "&a&l✔");
        else return this.config.getString("disabled-msg", "&c&l✘");
    }

    public String getMaterial(boolean toggleState) {
        if (toggleState) return this.config.getString("enabled-icon", "TROPICAL_FISH");
        else return this.config.getString("disabled-icon", "SALMON");
    }

    public List<Button> getButtons(@NotNull final UUID uuid) {
        List<Button> buttons = new ArrayList<>();
        ConfigurationSection section = this.config.getConfigurationSection("main-menu");
        if (section == null) return buttons;
        for (String value : section.getKeys(false)) {
            Message materialIcon = new Message(this.config.getString("main-menu." + value + ".item", "BARRIER"));
            materialIcon.setToggleIcon(getMaterial(!EvenMoreFish.disabledPlayers.contains(uuid)));
            Button button = new Button(
                    value,
                    uuid,
                    materialIcon.getRawMessage(false, true),
                    this.config.getString("main-menu." + value + ".name"),
                    this.config.getStringList("main-menu." + value + ".lore"),
                    this.config.getInt("main-menu." + value + ".slot", -1)
            );
            buttons.add(button);
        }
        return buttons;
    }
}
