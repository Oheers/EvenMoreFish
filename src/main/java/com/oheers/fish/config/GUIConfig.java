package com.oheers.fish.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.gui.Button;
import com.oheers.fish.gui.FillerStyle;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class GUIConfig {

    private final EvenMoreFish plugin;
    private FileConfiguration config;
    public HashMap<Integer, ItemStack> fillerDefault = new HashMap<>();

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

        fillerDefault.clear();
        generateDefaultFiller();
    }

    public String getToggle(boolean toggleState) {
        if (toggleState) return this.config.getString("enabled-msg", "&a&l✔");
        else return this.config.getString("disabled-msg", "&c&l✘");
    }

    public String getMaterial(boolean toggleState) {
        if (toggleState) return this.config.getString("enabled-icon", "TROPICAL_FISH");
        else return this.config.getString("disabled-icon", "SALMON");
    }

    public String getGUIName(String gui) {
        return this.config.getString(gui + ".title", "&#55aaffEvenMoreFish GUI");
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

    /**
     * Fetches the filler style to be used in place of empty slots in the GUI.
     *
     * @param menuID The ID of the menu to find its filler style for.
     * @return The filler style found. An error message is sent if an invalid one is set in config.
     */
    public FillerStyle getFillerStyle(@NotNull final String menuID) {
        try {
            return FillerStyle.valueOf(this.config.getString(menuID + ".filler-layout", "DEFAULT").toUpperCase());
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, this.config.getString(menuID + ".filler-layout") + " is not a valid filler layout for the " + menuID);
            return FillerStyle.DEFAULT;
        }
    }

    public void generateDefaultFiller() {
        InputStream fillerSchematicsStream = this.getClass().getClassLoader().getResourceAsStream("guilayouts.json");
        JsonObject jsonObject = (JsonObject) JsonParser.parseReader(new InputStreamReader(fillerSchematicsStream));
        JsonObject relevantSchematic = (JsonObject) jsonObject.get(getFillerStyle("main-menu").toString().toLowerCase());
        JsonArray layoutArray = (JsonArray) relevantSchematic.get("layout");
        JsonArray planArray = (JsonArray) relevantSchematic.get("plan");
        for (int i=0; i < layoutArray.size(); i++) {
            String line = layoutArray.get(i).getAsString();
            for (int j = 0; j < layoutArray.get(i).getAsString().length(); j++) {
                char materialID = line.charAt(j);
                if (materialID != 'X') {
                    fillerDefault.put(((i * 9) + j), new ItemStack(Material.valueOf(planArray.get(Integer.parseInt(String.valueOf(materialID))).getAsString())));
                }
            }
        }
    }
}
