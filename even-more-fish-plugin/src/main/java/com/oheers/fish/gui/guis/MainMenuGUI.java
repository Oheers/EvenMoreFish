package com.oheers.fish.gui.guis;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.GUIFillerConfig;
import com.oheers.fish.gui.GUIUtils;
import de.themoep.inventorygui.InventoryGui;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class MainMenuGUI implements EMFGUI {

    private final InventoryGui gui;
    private final HumanEntity viewer;

    public MainMenuGUI(@NotNull HumanEntity viewer) {
        this.viewer = viewer;
        Section section = GUIConfig.getInstance().getConfig().getSection("main-menu");
        gui = GUIUtils.createGUI(section);
        if (section == null) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Could not find the config for the Main Menu GUI!");
            return;
        }
        gui.setFiller(GUIUtils.getFillerItem(section.getString("filler"), Material.BLACK_STAINED_GLASS_PANE));
        gui.addElements(GUIUtils.getElements(section, this, null));
        gui.addElements(GUIFillerConfig.getInstance().getDefaultFillerElements());
    }

    @Override
    public void open() {
        gui.show(this.viewer);
    }

    @Override
    public InventoryGui getGui() {
        return this.gui;
    }

}
