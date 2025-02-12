package com.oheers.fish.gui.guis.journal;

import com.oheers.fish.FishUtils;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIUtils {

    public static InventoryGui createGUI(Section section) {
        String[] guiSetup = section.getStringList("setup").toArray(new String[0]);
        String title = section.getString("title", "GUI");
        return new InventoryGui(null, title, guiSetup);
    }

    public static ItemStack getFillerItem(String materialName, Material defaultMaterial) {
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = defaultMaterial;
        }
        return new ItemStack(material);
    }

    public static GuiElement[] getElements(Section section, Object... params) {
        List<GuiElement> elements = new ArrayList<>();
        Section elementsSection = section.getSection("elements");
        if (elementsSection != null) {
            for (String key : elementsSection.getRoutesAsStrings(false)) {
                Section elementSection = elementsSection.getSection(key);
                if (elementSection != null) {
                    elements.add(createElement(elementSection, params));
                }
            }
        }
        return elements.toArray(new GuiElement[0]);
    }

    private static GuiElement createElement(Section section, Object... params) {
        char slotChar = section.getString("slot").charAt(0);
        Material material = Material.matchMaterial(section.getString("material", "STONE"));
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(FishUtils.translateColorCodes(section.getString("display-name", "")));
            List<String> lore = section.getStringList("lore");
            if (lore != null) {
                List<String> translatedLore = new ArrayList<>();
                for (String line : lore) {
                    translatedLore.add(FishUtils.translateColorCodes(line));
                }
                meta.setLore(translatedLore);
            }
            itemStack.setItemMeta(meta);
        }
        return new StaticGuiElement(slotChar, itemStack);
    }
}