package com.oheers.fish.gui.guis.journal;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.GUIFillerConfig;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.GUIUtils;
import com.oheers.fish.gui.guis.EMFGUI;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class FishCodexGUI implements EMFGUI {

    private final InventoryGui gui;
    private final HumanEntity viewer;
    private final Rarity rarity;

    public FishCodexGUI(@NotNull HumanEntity viewer, @NotNull Rarity rarity) {
        this.viewer = viewer;
        this.rarity = rarity;
        Section section = GUIConfig.getInstance().getConfig().getSection("journal-rarity");
        this.gui = GUIUtils.createGUI(section);
        if (section == null) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Could not find the config for the Fish Journal GUI!");
            return;
        }
        this.gui.setFiller(GUIUtils.getFillerItem(section.getString("filler"), Material.BLACK_STAINED_GLASS_PANE));
        this.gui.addElements(GUIUtils.getElements(section, this, null));
        this.gui.addElements(GUIFillerConfig.getInstance().getDefaultFillerElements());
        this.gui.addElement(getFishGroup(section));
    }

    private DynamicGuiElement getFishGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("fish-character", "f"), 'f');

        return new DynamicGuiElement(character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            this.rarity.getFishList().forEach(fish ->
                group.addElement(new StaticGuiElement(character, getFishItem(fish)))
            );
            return group;
        });
    }

    private ItemStack getFishItem(Fish fish) {
        // TODO respect configs
        return fish.give(-1);
    }

    @Override
    public InventoryGui getGui() {
        return gui;
    }

    @Override
    public void open() {
        gui.show(viewer);
    }

    @Override
    public void doRescue() {}

}
