package com.oheers.fish.utils;

import co.aikar.commands.CommandHelp;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.gui.MainMenuGUI;
import com.oheers.fish.selling.SellGUI;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GUIUtils {

    private static Map<String, GuiElement.Action> actionMap = null;

    public static ItemStack getExitItem() {
        FileConfiguration config = GUIConfig.getInstance().getConfig();
        return createItemStack(
                config.getString("gui.global.exit.material", "structure_void"),
                Material.STRUCTURE_VOID,
                config.getString("gui.global.exit.name", "&cExit"),
                config.getStringList("gui.global.exit.lore")
        );
    }

    public static GuiPageElement getFirstPageButton() {
        FileConfiguration config = GUIConfig.getInstance().getConfig();
        return new GuiPageElement('f',
                createItemStack(
                        config.getString("gui.global.first-page.material", "arrow"),
                        Material.ARROW,
                        config.getString("gui.global.first-page.name", "&bFirst Page"),
                        config.getStringList("gui.global.first-page.lore")
                ),
                GuiPageElement.PageAction.FIRST
        );
    }

    public static GuiPageElement getNextPageButton() {
        FileConfiguration config = GUIConfig.getInstance().getConfig();
        return new GuiPageElement('n',
                createItemStack(
                        config.getString("gui.global.next-page.material", "paper"),
                        Material.PAPER,
                        config.getString("gui.global.next-page.name", "&bNext Page"),
                        config.getStringList("gui.global.next-page.lore")
                ),
                GuiPageElement.PageAction.NEXT
        );
    }

    public static GuiPageElement getPreviousPageButton() {
        FileConfiguration config = GUIConfig.getInstance().getConfig();
        return new GuiPageElement('p',
                createItemStack(
                        config.getString("gui.global.previous-page.material", "paper"),
                        Material.PAPER,
                        config.getString("gui.global.previous-page.name", "&bPrevious Page"),
                        config.getStringList("gui.global.previous-page.lore")
                ),
                GuiPageElement.PageAction.PREVIOUS
        );
    }

    public static GuiPageElement getLastPageButton() {
        FileConfiguration config = GUIConfig.getInstance().getConfig();
        return new GuiPageElement('l',
                createItemStack(
                        config.getString("gui.global.last-page.material", "arrow"),
                        Material.ARROW,
                        config.getString("gui.global.last-page.name", "&bLast Page"),
                        config.getStringList("gui.global.last-page.lore")
                ),
                GuiPageElement.PageAction.LAST
        );
    }

    public static GuiPageElement[] getPageElements() {
        return new GuiPageElement[]{
                getFirstPageButton(),
                getPreviousPageButton(),
                getNextPageButton(),
                getLastPageButton()
        };
    }

    public static ItemStack createItemStack(@NotNull String materialName, @NotNull Material defaultMaterial, @NotNull String display, @NotNull List<String> lore) {
        return new ItemBuilder(materialName, defaultMaterial)
                .withDisplay(display)
                .withLore(lore)
                .build();
    }

    public static InventoryGui createGUI(@Nullable ConfigurationSection section) {
        if (section == null) {
            return new InventoryGui(
                    EvenMoreFish.getInstance(),
                    new Message("&cBroken GUI! Please tell an admin!").getRawMessage(false),
                    new String[0]
            );
        }
        return new InventoryGui(
                EvenMoreFish.getInstance(),
                new Message(section.getString("title", "EvenMoreFish Inventory")).getRawMessage(false),
                section.getStringList("layout").toArray(new String[0])
        );
    }

    public static ItemStack getFillerItem(@Nullable String materialName, @NotNull Material defaultMaterial) {
        Material material = ItemUtils.getMaterial(materialName, defaultMaterial);
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        meta.setDisplayName("");
        stack.setItemMeta(meta);
        return stack;
    }

    public static StaticGuiElement getStaticElement(@NotNull String configLocation, @NotNull ConfigurationSection section) {
        ItemFactory factory = new ItemFactory(configLocation, section);
        factory.enableAllChecks();
        // Get ItemStack
        ItemStack item = factory.createItem(null, -1);
        // Get Character
        char character = section.getString("character", "#").toCharArray()[0];
        // Get Click Action
        GuiElement.Action action = getActionMap().get(section.getString("click-action", "none"));
        // Create Element
        return new StaticGuiElement(character, item, action);
    }

    public static StaticGuiElement getStaticElement(@NotNull ConfigurationSection section) {
        ItemFactory factory = new ItemFactory(null, section);
        factory.enableAllChecks();
        // Get ItemStack
        ItemStack item = factory.createItem(null, -1);
        // Get Character
        char character;
        try {
            character = section.getString("character", "#").toCharArray()[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            character = '#';
        }
        // Get Click Action
        GuiElement.Action action = getActionMap().get(section.getString("click-action", "none"));
        // Create Element
        return new StaticGuiElement(character, item, action);
    }

    public static List<GuiElement> getElements(@NotNull ConfigurationSection section) {
        return section.getKeys(false)
                .stream()
                .map(section::getConfigurationSection)
                .filter(Objects::nonNull)
                // Exclude non-item config sections, if there are any
                .filter(loopSection -> loopSection.getKeys(false).contains("item"))
                .map(GUIUtils::getStaticElement)
                .collect(Collectors.toList());
    }

    public static Map<String, GuiElement.Action> getActionMap() {
        if (actionMap != null) {
            return actionMap;
        }
        Map<String, GuiElement.Action> newActionMap = new HashMap<>();
        // Exiting the main menu should close the GUI
        newActionMap.put("full-exit", click -> {
            click.getGui().close();
            return true;
        });
        // Exiting a sub-menu should open the main menu
        newActionMap.put("open-main-menu", click -> {
            new MainMenuGUI(click.getWhoClicked()).open();
            return true;
        });
        // Toggling custom fish should redraw the GUI and leave it at that
        newActionMap.put("fish-toggle", click -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (EvenMoreFish.getInstance().getDisabledPlayers().contains(humanEntity.getUniqueId())) {
                EvenMoreFish.getInstance().getDisabledPlayers().remove(humanEntity.getUniqueId());
                new Message(ConfigMessage.TOGGLE_ON).broadcast(humanEntity, false);
            } else {
                EvenMoreFish.getInstance().getDisabledPlayers().add(humanEntity.getUniqueId());
                new Message(ConfigMessage.TOGGLE_OFF).broadcast(humanEntity, false);
            }
            click.getGui().draw();
            return true;
        });
        // The shop action should just open the shop menu
        newActionMap.put("open-shop", click -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (humanEntity instanceof Player) {
                Player player = (Player) humanEntity;
                new SellGUI(player).open();
            }
            return true;
        });
        newActionMap.put("show-command-help", click -> {
            Bukkit.dispatchCommand(click.getWhoClicked(), "emf help");
            return true;
        });
        actionMap = newActionMap;
        return newActionMap;
    }

}
