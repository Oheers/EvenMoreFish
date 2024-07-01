package com.oheers.fish.gui;

import com.oheers.fish.config.messages.Message;
import com.oheers.fish.utils.ItemUtils;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a GUI
 */
public class GUIBuilder {

    private @NotNull String title;
    private @NotNull String[] layout;
    private @NotNull Material fillerItem;
    private final @NotNull List<GuiElement> elements;
    private @Nullable InventoryGui.CloseAction closeAction;

    /**
     * Build a GUI from preset values.
     * @param title The GUI Title
     * @param layout The GUI Layout
     * @param fillerItem The GUI's Filler Item
     */
    public GUIBuilder(@NotNull String title, @NotNull String[] layout, @NotNull Material fillerItem, @Nullable InventoryGui.CloseAction closeAction) {
        this.title = new Message(title).getRawMessage(false);
        this.layout = layout;
        this.elements = new ArrayList<>();
        this.fillerItem = fillerItem;
        this.closeAction = closeAction;
    }

    /**
     * Builds a GUI from a ConfigurationSection
     * @param section The ConfigurationSection for this GUI
     */
    public GUIBuilder(@NotNull ConfigurationSection section) {
        this.title = new Message(section.getString("title", "Inventory")).getRawMessage(false);
        this.layout = section.getStringList("layout").toArray(new String[0]);
        this.elements = new ArrayList<>();
        this.fillerItem = ItemUtils.getMaterial(section.getString("filler"), Material.GRAY_STAINED_GLASS_PANE);
    }

    public void addElement(@NotNull GuiElement element) {
        elements.add(element);
    }

    public void addElements(@NotNull List<GuiElement> elements) {
        elements.forEach(this::addElement);
    }

    public void addElements(@NotNull GuiElement... elements) {
        for (GuiElement element : elements) {
            addElement(element);
        }
    }

    public void removeElement(@NotNull GuiElement element) {
        elements.remove(element);
    }

    public void clearElements() {
        elements.clear();
    }

    public @NotNull String getTitle() {
        return this.title;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public @NotNull String[] getLayout() {
        return this.layout;
    }

    public void setLayout(@NotNull String[] layout) {
        this.layout = layout;
    }

    public @NotNull Material getFillerItem() {
        return this.fillerItem;
    }

    public void setFillerItem(@NotNull Material fillerItem) {
        this.fillerItem = fillerItem;
    }

    public void setCloseAction(@Nullable InventoryGui.CloseAction closeAction) {
        this.closeAction = closeAction;
    }

    public InventoryGui buildGui(@NotNull Plugin plugin) {
        InventoryGui gui = new InventoryGui(plugin, title, layout);
        gui.setFiller(new ItemStack(fillerItem));
        gui.addElements(elements);
        if (closeAction != null) {
            gui.setCloseAction(closeAction);
        }
        return gui;
    }

}
