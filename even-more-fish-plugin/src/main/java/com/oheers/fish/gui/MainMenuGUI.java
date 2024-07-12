package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.GUIFillerConfig;
import com.oheers.fish.utils.GUIUtils;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class MainMenuGUI {

    private final InventoryGui gui;
    private final HumanEntity viewer;

    public MainMenuGUI(@NotNull HumanEntity viewer) {
        this.viewer = viewer;
        ConfigurationSection section = GUIConfig.getInstance().getConfig().getConfigurationSection("main-menu");
        gui = GUIUtils.createGUI(section);
        if (section == null) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Could not find the config for the Main Menu GUI!");
            return;
        }
        gui.setFiller(GUIUtils.getFillerItem(section.getString("filler"), Material.BLACK_STAINED_GLASS_PANE));
        gui.addElements(GUIUtils.getElements(section, null));
        gui.addElements(GUIFillerConfig.getInstance().getDefaultFillerElements());
    }

    public void open() {
        gui.show(this.viewer);
    }

}
