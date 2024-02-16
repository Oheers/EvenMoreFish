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

public class GUIConfig extends ConfigBase {

    private static GUIConfig instance = null;
    public HashMap<Integer, ItemStack> fillerDefault = new HashMap<>();
    public FillerStyle guiFillerStyle;

    public GUIConfig() {
        super("guis.yml");
        reload();
        instance = this;
    }
    
    public static GUIConfig getInstance() {
        return instance;
    }
    
    @Override
    public void reload() {
        super.reload();
        fillerDefault.clear();
        generateDefaultFiller();
        guiFillerStyle = getFillerStyle("main-menu");
    }

    public String getToggle(boolean toggleState) {
        if (toggleState) return getConfig().getString("enabled-msg", "&a&l✔");
        else return getConfig().getString("disabled-msg", "&c&l✘");
    }

    public String getMaterial(boolean toggleState) {
        if (toggleState) return getConfig().getString("enabled-icon", "TROPICAL_FISH");
        else return getConfig().getString("disabled-icon", "SALMON");
    }

    public String getGUIName(String gui) {
        return getConfig().getString(gui + ".title", "&#55aaffEvenMoreFish GUI");
    }

    public List<Button> getButtons(@NotNull final UUID uuid) {
        List<Button> buttons = new ArrayList<>();
        ConfigurationSection section = getConfig().getConfigurationSection("main-menu");
        if (section == null) return buttons;
        for (String value : section.getKeys(false)) {
            Message materialIcon = new Message(getConfig().getString("main-menu." + value + ".item", "BARRIER"));
            materialIcon.setToggleIcon(getMaterial(!EvenMoreFish.disabledPlayers.contains(uuid)));
            Button button = new Button(
                    value,
                    uuid,
                    materialIcon.getRawMessage(false, true),
                    getConfig().getString("main-menu." + value + ".name"),
                    getConfig().getStringList("main-menu." + value + ".lore"),
                    getConfig().getInt("main-menu." + value + ".slot", -1)
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
            return FillerStyle.valueOf(getConfig().getString(menuID + ".filler-layout", "DEFAULT").toUpperCase());
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, getConfig().getString(menuID + ".filler-layout") + " is not a valid filler layout for the " + menuID);
            return FillerStyle.DEFAULT;
        }
    }

    public void generateDefaultFiller() {
        String layouts = "";
        try {
            Material.valueOf("GRASS");
            layouts = "guilayouts.json";
        } catch (IllegalArgumentException ex) {
            layouts = "guilayouts-1.20.3.json";
        }
        InputStream fillerSchematicsStream = this.getClass().getClassLoader().getResourceAsStream(layouts);
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
