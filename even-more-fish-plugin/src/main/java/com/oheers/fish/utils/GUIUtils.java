package com.oheers.fish.utils;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.gui.EMFGUI;
import com.oheers.fish.gui.MainMenuGUI;
import com.oheers.fish.gui.SellGUI;
import com.oheers.fish.selling.SellHelper;
import de.themoep.inventorygui.*;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GUIUtils {

    private static Map<String, GuiElement.Action> externalActionMap;

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

    public static GuiElement getDynamicElement(@NotNull String configLocation, @NotNull ConfigurationSection section, @Nullable EMFGUI gui, @Nullable Supplier<Map<String, String>> replacementSupplier) {
        // Get Character
        char character = FishUtils.getCharFromString(section.getString("character", "#"), '#');

        // Create Element
        return new DynamicGuiElement(character, (viewer) -> {
            ItemFactory factory = new ItemFactory(configLocation, section);
            factory.enableAllChecks();
            // Get ItemStack
            ItemStack item;
            if (replacementSupplier == null) {
                item = factory.createItem(null, -1, null);
            } else {
                item = factory.createItem(null, -1, replacementSupplier.get());
            }
            // Get Click Action
            GuiElement.Action action = getActionMap(gui).get(section.getString("click-action", "none"));
            return new StaticGuiElement(character, item, action);
        });
    }

    public static DynamicGuiElement getDynamicElement(@NotNull ConfigurationSection section, @Nullable EMFGUI gui, @Nullable Supplier<Map<String, String>> replacementSupplier) {
        // Get Character
        char character = FishUtils.getCharFromString(section.getString("character", "#"), '#');

        // Create Element
        return new DynamicGuiElement(character, (viewer) -> {
            ItemFactory factory = new ItemFactory(null, section);
            factory.enableAllChecks();
            // Get ItemStack
            ItemStack item;
            if (replacementSupplier == null) {
                item = factory.createItem(null, -1, null);
            } else {
                item = factory.createItem(null, -1, replacementSupplier.get());
            }
            // Get Click Action
            GuiElement.Action action = getActionMap(gui).get(section.getString("click-action", "none"));
            return new StaticGuiElement(character, item, action);
        });
    }

    public static List<GuiElement> getElements(@NotNull ConfigurationSection section, @Nullable EMFGUI gui, @Nullable Supplier<Map<String, String>> replacementSupplier) {
        return section.getKeys(false)
                .stream()
                .map(section::getConfigurationSection)
                .filter(Objects::nonNull)
                // Exclude non-item config sections, if there are any
                .filter(loopSection -> loopSection.getKeys(false).contains("item"))
                .map(loopSection -> GUIUtils.getDynamicElement(loopSection, gui, replacementSupplier))
                .collect(Collectors.toList());
    }

    public static boolean addAction(@NotNull String actionKey, @NotNull GuiElement.Action action) {
        if (externalActionMap == null) {
            externalActionMap = new HashMap<>();
        }
        if (externalActionMap.containsKey(actionKey)) {
            return false;
        }
        externalActionMap.put(actionKey, action);
        return true;
    }

    public static Map<String, GuiElement.Action> getActionMap(@Nullable EMFGUI gui) {
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

            if (!(humanEntity instanceof Player)) {
                return true;
            }
            Player player = (Player) humanEntity;
            new SellGUI(player, SellGUI.SellState.NORMAL, null).open();
            return true;
        });
        newActionMap.put("show-command-help", click -> {
            Bukkit.dispatchCommand(click.getWhoClicked(), "emf help");
            return true;
        });
        newActionMap.put("sell-inventory", click -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (!(humanEntity instanceof Player)) {
                return true;
            }
            Player player = (Player) humanEntity;
            if (gui instanceof SellGUI) {
                SellGUI sellGUI = (SellGUI) gui;
                new SellGUI(player, SellGUI.SellState.CONFIRM, sellGUI.getFishInventory()).open();
                return true;
            }
            new SellHelper(click.getWhoClicked().getInventory(), player).sellFish();
            click.getGui().close();
            return true;
        });
        newActionMap.put("sell-shop", click -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (gui instanceof SellGUI && humanEntity instanceof Player) {
                SellGUI sellGUI = (SellGUI) gui;
                Player player = (Player) humanEntity;
                new SellGUI(player, SellGUI.SellState.CONFIRM, sellGUI.getFishInventory()).open();
                return true;
            }
            SellHelper.sellInventoryGui(click.getGui(), click.getWhoClicked());
            click.getGui().close();
            return true;
        });
        newActionMap.put("sell-inventory-confirm", click -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (!(humanEntity instanceof Player)) {
                return true;
            }
            Player player = (Player) humanEntity;
            new SellHelper(click.getWhoClicked().getInventory(), player).sellFish();
            click.getGui().close();
            return true;
        });
        newActionMap.put("sell-shop-confirm", click -> {
            SellHelper.sellInventoryGui(click.getGui(), click.getWhoClicked());
            click.getGui().close();
            return true;
        });
        if (externalActionMap != null) {
            newActionMap.putAll(externalActionMap);
        }
        return newActionMap;
    }

}
