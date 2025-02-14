package com.oheers.fish.gui.guis.journal;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.GUIFillerConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.model.FishReport;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.GUIUtils;
import com.oheers.fish.gui.guis.EMFGUI;
import com.oheers.fish.utils.ItemBuilder;
import com.oheers.fish.utils.ItemFactory;
import com.oheers.fish.utils.ItemUtils;
import de.themoep.inventorygui.*;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.impl.QOM;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class FishJournalGui implements EMFGUI {

    private final InventoryGui gui;
    private final HumanEntity viewer;
    private final Rarity rarity;

    public FishJournalGui(@NotNull HumanEntity viewer, @Nullable Rarity rarity) {
        this.viewer = viewer;
        this.rarity = rarity;

        String configPath = (rarity == null ? "journal-menu" : "journal-rarity");

        Section section = GUIConfig.getInstance().getConfig().getSection(configPath);
        this.gui = GUIUtils.createGUI(section);
        if (section == null) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Could not find the config for the Fish Journal GUI!");
            return;
        }

        if (rarity != null) {
            AbstractMessage title = EvenMoreFish.getAdapter().createMessage(this.gui.getTitle());
            title.setRarity(rarity.getDisplayName());
            title.setRarityColour(rarity.getColour());
            this.gui.setTitle(title.getLegacyMessage());
        }

        this.gui.setFiller(GUIUtils.getFillerItem(section.getString("filler"), Material.BLACK_STAINED_GLASS_PANE));
        this.gui.addElements(GUIUtils.getElements(section, this, null));
        this.gui.addElements(GUIFillerConfig.getInstance().getDefaultFillerElements());
        this.gui.addElement(getGroup(section));
    }

    private DynamicGuiElement getGroup(Section section) {
        return (this.rarity == null ? getRarityGroup(section) : getFishGroup(section));
    }

    private DynamicGuiElement getFishGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("fish-character", "f"), 'f');

        return new DynamicGuiElement(character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            this.rarity.getFishList().forEach(fish ->
                group.addElement(new StaticGuiElement(character, getFishItem(fish, section)))
            );
            return group;
        });
    }

    private ItemStack getFishItem(Fish fish, Section section) {
        Database database = EvenMoreFish.getInstance().getDatabase();

        boolean hideUndiscovered = section.getBoolean("hide-undiscovered-fish", true);
        // If undiscovered fish should be hidden
        if (hideUndiscovered && !database.userHasFish(fish, viewer)) {
            ItemFactory factory = new ItemFactory("undiscovered-fish", section);
            factory.enableAllChecks();
            return factory.createItem(null, -1);
        }

        ItemFactory factory = new ItemFactory("fish-item", section);
        factory.enableAllChecks();
        ItemStack item = factory.createItem(null, -1);

        FishUtils.editMeta(item, meta -> {
            // Display Name
            String displayStr = section.getString("fish-item.item.displayname");
            if (displayStr != null) {
                AbstractMessage display = EvenMoreFish.getAdapter().createMessage(displayStr);
                display.setVariable("{fishname}", fish.getDisplayName());
                meta.setDisplayName(display.getLegacyMessage());
            }

            // Lore
            LocalDateTime discover = database.getFirstCatchDateForPlayer(fish, viewer);
            String discoverStr = discover == null ? null : discover.format(DateTimeFormatter.ISO_DATE);

            AbstractMessage lore = EvenMoreFish.getAdapter().createMessage(
                section.getStringList("fish-item.lore")
            );
            lore.setVariable("{times-caught}", Integer.toString(database.getAmountFishCaughtForPlayer(fish, viewer)), "Unknown");
            lore.setVariable("{largest-size}", database.getLargestFishSizeForPlayer(fish, viewer), "Unknown");
            lore.setVariable("{discover-date}", discoverStr, "Unknown");
            lore.setVariable("{discoverer}", FishUtils.getPlayerName(database.getDiscoverer(fish)), "Unknown");
            lore.setVariable("{server-largest}", database.getLargestFishSize(fish), "Unknown");
            lore.setVariable("{server-caught}", database.getAmountFishCaught(fish), "Unknown");
            meta.setLore(lore.getLegacyListMessage());
        });

        ItemUtils.changeMaterial(item, fish.getFactory().getMaterial());

        return item;
    }

    private DynamicGuiElement getRarityGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("rarity-character", "r"), 'r');

        return new DynamicGuiElement(character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            FishManager.getInstance().getRarityMap().values().forEach(rarity -> {
                ItemFactory factory = new ItemFactory("rarity-item", section);
                factory.enableAllChecks();
                ItemStack item = factory.createItem(null, -1);
                item = ItemUtils.changeMaterial(item, rarity.getMaterial());

                FishUtils.editMeta(item, meta -> {
                    String displayStr = meta.getDisplayName();
                    AbstractMessage display = EvenMoreFish.getAdapter().createMessage(displayStr);
                    display.setRarity(rarity.getDisplayName());
                    display.setRarityColour(rarity.getColour());
                    meta.setDisplayName(display.getLegacyMessage());
                });

                group.addElement(new StaticGuiElement(character, item, click -> {
                    click.getGui().close();
                    new FishJournalGui(viewer, rarity).open();
                    return true;
                }));
            });
            return group;
        });
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
