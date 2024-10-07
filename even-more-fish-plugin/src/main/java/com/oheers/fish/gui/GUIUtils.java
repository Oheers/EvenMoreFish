package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.gui.guis.BaitsGUI;
import com.oheers.fish.gui.guis.EMFGUI;
import com.oheers.fish.gui.guis.MainMenuGUI;
import com.oheers.fish.gui.guis.SellGUI;
import com.oheers.fish.selling.SellHelper;
import com.oheers.fish.utils.ItemBuilder;
import com.oheers.fish.utils.ItemFactory;
import com.oheers.fish.utils.ItemUtils;
import com.sun.jdi.PrimitiveValue;
import de.themoep.inventorygui.*;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GUIUtils {

    private static Map<String, GuiElement.Action> externalActionMap;

    public static GuiPageElement getFirstPageButton() {
        YamlDocument config = GUIConfig.getInstance().getConfig();
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
        YamlDocument config = GUIConfig.getInstance().getConfig();
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
        YamlDocument config = GUIConfig.getInstance().getConfig();
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
        YamlDocument config = GUIConfig.getInstance().getConfig();
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

    public static InventoryGui createGUI(@Nullable Section section) {
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

    public static GuiElement getElement(@Nullable String configLocation, @NotNull Section section, @Nullable EMFGUI gui, @Nullable Supplier<Map<String, String>> replacementSupplier) {
        // Get Character
        char character = FishUtils.getCharFromString(section.getString("character", "#"), '#');

        Map<ClickType, String> clickTypeStringMap = new HashMap<>();
        if (section.isSection("click-action")) {
            clickTypeStringMap.put(ClickType.LEFT, section.getString("click-action.left", "none"));
            clickTypeStringMap.put(ClickType.RIGHT, section.getString("click-action.right", "none"));
        } else {
            clickTypeStringMap.put(ClickType.LEFT, section.getString("click-action", "none"));
        }
        List<String> commands = section.getStringList("click-commands");

        ItemFactory factory = new ItemFactory(configLocation, section);
        factory.enableAllChecks();
        // Get ItemStack
        ItemStack item;
        if (replacementSupplier == null) {
            item = factory.createItem(null, -1, null);
        } else {
            item = factory.createItem(null, -1, replacementSupplier.get());
        }

        // Create the element
        if (eitherClickTypeMatchesString(clickTypeStringMap, "next-page")) {
            return new GuiPageElement(character, item, GuiPageElement.PageAction.NEXT);
        } else if (eitherClickTypeMatchesString(clickTypeStringMap, "first-page")) {
            return new GuiPageElement(character, item, GuiPageElement.PageAction.FIRST);
        } else if (eitherClickTypeMatchesString(clickTypeStringMap, "previous-page")) {
            return new GuiPageElement(character, item, GuiPageElement.PageAction.PREVIOUS);
        } else if (eitherClickTypeMatchesString(clickTypeStringMap, "last-page")) {
            return new GuiPageElement(character, item, GuiPageElement.PageAction.LAST);
        } else {
            return new DynamicGuiElement(character, (viewer) -> {
                GuiElement.Action left = getActionMap(gui).get(clickTypeStringMap.get(ClickType.LEFT));
                GuiElement.Action right = getActionMap(gui).get(clickTypeStringMap.get(ClickType.RIGHT));
                return new StaticGuiElement(character, item, click -> {
                    HumanEntity sender = click.getWhoClicked();
                    commands.forEach(command -> Bukkit.dispatchCommand(sender, command));
                    if (click.getType().equals(ClickType.LEFT) && left != null) {
                        return left.onClick(click);
                    } else if (click.getType().equals(ClickType.RIGHT) && right != null) {
                        return right.onClick(click);
                    }
                    return true;
                });
            });
        }
    }


    private static boolean eitherClickTypeMatchesString(@NotNull Map<ClickType, String> map, @NotNull String matcher) {
        boolean matches = true;
        for (String string : map.values()) {
            if (!string.equalsIgnoreCase(matcher)) {
                matches = false;
                break;
            }
        }
        return matches;
    }

    public static List<GuiElement> getElements(@NotNull Section section, @Nullable EMFGUI gui, @Nullable Supplier<Map<String, String>> replacementSupplier) {
        return section.getRoutesAsStrings(false)
                .stream()
                .map(section::getSection)
                .filter(Objects::nonNull)
                // Exclude non-item config sections, if there are any
                .filter(loopSection -> loopSection.getRoutesAsStrings(false).contains("item"))
                .map(loopSection -> GUIUtils.getElement(null, loopSection, gui, replacementSupplier))
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
            if (gui instanceof SellGUI sellGUI) {
                sellGUI.doRescue();
            }
            click.getGui().close();
            return true;
        });
        // Exiting a sub-menu should open the main menu
        newActionMap.put("open-main-menu", click -> {
            if (gui instanceof SellGUI sellGUI) {
                sellGUI.doRescue();
            }
            new MainMenuGUI(click.getWhoClicked()).open();
            return true;
        });
        // Toggling custom fish should redraw the GUI and leave it at that
        newActionMap.put("fish-toggle", click -> {
            if (click.getWhoClicked() instanceof Player player) {
                EvenMoreFish.getInstance().performFishToggle(player);
            }
            click.getGui().draw();
            return true;
        });
        // The shop action should just open the shop menu
        newActionMap.put("open-shop", click -> {

            if (gui instanceof SellGUI sellGUI) {
                sellGUI.doRescue();
            }

            HumanEntity humanEntity = click.getWhoClicked();

            if (!(humanEntity instanceof Player player)) {
                return true;
            }
            new SellGUI(player, SellGUI.SellState.NORMAL, null).open();
            return true;
        });
        newActionMap.put("show-command-help", click -> {
            Bukkit.dispatchCommand(click.getWhoClicked(), "emf help");
            return true;
        });
        newActionMap.put("sell-inventory", click -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (!(humanEntity instanceof Player player)) {
                return true;
            }
            if (gui instanceof SellGUI sellGUI) {
                new SellGUI(player, SellGUI.SellState.CONFIRM, sellGUI.getFishInventory()).open();
                return true;
            }
            new SellHelper(click.getWhoClicked().getInventory(), player).sellFish();
            click.getGui().close();
            return true;
        });
        newActionMap.put("sell-shop", click -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (gui instanceof SellGUI sellGUI && humanEntity instanceof Player player) {
                new SellGUI(player, SellGUI.SellState.CONFIRM, sellGUI.getFishInventory()).open();
                return true;
            }
            SellHelper.sellInventoryGui(click.getGui(), click.getWhoClicked());
            click.getGui().close();
            return true;
        });
        newActionMap.put("sell-inventory-confirm", click -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (!(humanEntity instanceof Player player)) {
                return true;
            }
            new SellHelper(click.getWhoClicked().getInventory(), player).sellFish();
            if (gui instanceof SellGUI sellGUI) {
                sellGUI.doRescue();
            }
            click.getGui().close();
            return true;
        });
        newActionMap.put("sell-shop-confirm", click -> {
            SellHelper.sellInventoryGui(click.getGui(), click.getWhoClicked());
            click.getGui().close();
            return true;
        });
        newActionMap.put("open-baits-menu", click -> {
            if (gui instanceof SellGUI sellGUI) {
                sellGUI.doRescue();
            }
            new BaitsGUI(click.getWhoClicked()).open();
            return true;
        });
        // Add page actions so third party plugins cannot register their own.
        newActionMap.put("first-page", click -> true);
        newActionMap.put("previous-page", click -> true);
        newActionMap.put("next-page", click -> true);
        newActionMap.put("last-page", click -> true);
        if (externalActionMap != null) {
            newActionMap.putAll(externalActionMap);
        }
        return newActionMap;
    }

}
