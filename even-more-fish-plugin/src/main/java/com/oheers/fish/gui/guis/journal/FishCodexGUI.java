package com.oheers.fish.gui.guis.journal;

import com.oheers.fish.FishUtils;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.GUIUtils;
import com.oheers.fish.gui.guis.EMFGUI;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FishCodexGUI implements Listener, EMFGUI {

    private final DatabaseManager dbManager;
    private final EmfCodex plugin;
    private final YamlDocument guiConfig;
    private InventoryGui gui;
    private final Player viewer;
    private final String rarity;
    private final String dateFormat;

    public FishCodexGUI(DatabaseManager dbManager, EmfCodex plugin, Player viewer, String rarity) {
        this.dbManager = dbManager;
        this.plugin = plugin;
        this.viewer = viewer;
        this.rarity = rarity;
        this.dateFormat = plugin.getConfig().getString("date-format", "MMM dd, yyyy");
        this.guiConfig = GUIConfig.getInstance().getConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public InventoryGui getGui() {
        return gui;
    }

    public void open() {
        CompletableFuture.supplyAsync(() -> {
            // Fetch fish data from the database asynchronously
            List<FishData> caughtFishDataList = dbManager.getFishDataSync(viewer.getUniqueId());
            Map<String, FishData> caughtFishMap = new HashMap<>();
            for (FishData fishData : caughtFishDataList) {
                if (fishData.getRarity().equalsIgnoreCase(this.rarity)) {
                    caughtFishMap.put(fishData.getFishName(), fishData);
                }
            }
            return caughtFishMap;
        }).thenAccept(caughtFishMap -> {
            // Switch back to the main thread to build the GUI
            Bukkit.getScheduler().runTask(plugin, () -> {
                buildGUI(caughtFishMap);
                gui.show(viewer); // Open the GUI after everything is loaded
            });
        });
    }

    @Override
    public void doRescue() {}

    private void buildGUI(Map<String, FishData> caughtFishMap) {
        Section section = guiConfig.getSection("fish-codex-menu");
        if (section == null) {
            plugin.getLogger().severe("Could not find the config for the Fish Codex GUI!");
            return;
        }

        String menuTitle = FishUtils.translateColorCodes(section.getString("title", "{rarity} Fish Codex").replace("{rarity}", rarity));
        String[] layout = section.getStringList("layout").toArray(new String[0]);
        gui = new InventoryGui(plugin, viewer, menuTitle, layout);
        gui.setFiller(GUIUtils.getFillerItem(section.getString("filler", "GRAY_STAINED_GLASS_PANE"), Material.GRAY_STAINED_GLASS_PANE));
        gui.addElements(GUIUtils.getElements(section, this, null));

        // Add the fish items
        char character = FishUtils.getCharFromString(section.getString("fish-character", "f"), 'f');
        GuiElementGroup group = new GuiElementGroup(character);
        Map<String, Rarity> rarityMap = FishManager.getInstance().getRarityMap();
        Rarity rarity = rarityMap.get(this.rarity);

        if (rarity == null) {
            plugin.getLogger().warning("Rarity not found: " + this.rarity);
            return;
        }

        List<Fish> fishList = new ArrayList<>(rarity.getFishList());
        fishList.sort(Comparator.comparing(Fish::getName));

        for (Fish fish : fishList) {
            ItemStack item;
            ItemMeta meta;

            if (caughtFishMap.containsKey(fish.getName()) || viewer.hasPermission("emfCodex.showallfish")) {
                FishData fishData = caughtFishMap.getOrDefault(fish.getName(),
                        new FishData(fish.getName(), this.rarity, rarity.getColour(), 0, 0, 0, "", "", 0, 0, 0));
                item = fishData.getFishItem(fish.getName(), fish.getRarity().getId());
                meta = item.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(FishUtils.translateColorCodes(section.getString("fish-item.displayname", "{raritycolor}&l{rarity} &f{fishname}")
                            .replace("{raritycolor}", fishData.getColour())
                            .replace("{rarity}", fishData.getRarity().toUpperCase())
                            .replace("{fishname}", fishData.getFishName())));

                    List<String> lore = new ArrayList<>();
                    for (String line : section.getStringList("fish-item.lore")) {
                        line = line.replace("{raritycolor}", fishData.getColour())
                                .replace("{rarity}", fishData.getRarity())
                                .replace("{timescaught}", String.valueOf(fishData.getTimesCaught()))
                                .replace("{largestsize}", String.format("%.2f", fishData.getLargestSize()))
                                .replace("{shortestsize}", String.format("%.2f", fishData.getShortestSize()))
                                .replace("{discoverer}", fishData.getDiscoverer().isEmpty() ? "N/A" : fishData.getDiscoverer())
                                .replace("{serverbestsize}", String.format("%.2f", fishData.getServerBestSize()))
                                .replace("{servershortestsize}", String.format("%.2f", fishData.getServerShortestSize()))
                                .replace("{servercaught}", String.valueOf(fishData.getServerCaught()));

                        if (line.contains("{discoverdate}")) {
                            String discoverDate = fishData.getDiscoverDate();
                            if (!discoverDate.isEmpty()) {
                                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
                                try {
                                    Date date = inputFormat.parse(discoverDate);
                                    line = line.replace("{discoverdate}", outputFormat.format(date));
                                } catch (ParseException e) {
                                    line = line.replace("{discoverdate}", "N/A");
                                }
                            } else {
                                line = line.replace("{discoverdate}", "N/A");
                            }
                        }

                        if (line.contains("{best-player}")) {
                            String bestPlayer = dbManager.getBestPlayer(fish.getName()).join();
                            line = line.replace("{best-player}", bestPlayer != null ? bestPlayer : "N/A");
                        }

                        if (line.contains("{shortest-player}")) {
                            String shortestPlayer = dbManager.getShortestPlayer(fish.getName()).join();
                            line = line.replace("{shortest-player}", shortestPlayer != null ? shortestPlayer : "N/A");
                        }

                        if (line.contains("{most-caught-player}")) {
                            String mostCaughtPlayer = dbManager.getMostCaughtPlayer(fish.getName()).join();
                            line = line.replace("{most-caught-player}", mostCaughtPlayer != null ? mostCaughtPlayer : "N/A");
                        }

                        // Add the formatted lore line
                        lore.add(FishUtils.translateColorCodes(line));
                    }
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
            } else {
                item = new ItemStack(Material.valueOf(section.getString("undiscovered-fish.material", "IRON_BARS")));
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(FishUtils.translateColorCodes(section.getString("undiscovered-fish.displayname", "&8Undiscovered Fish")));
                    List<String> lore = section.getStringList("undiscovered-fish.lore");
                    if (!lore.isEmpty()) {
                        List<String> translatedLore = new ArrayList<>();
                        for (String line : lore) {
                            translatedLore.add(FishUtils.translateColorCodes(line));
                        }
                        meta.setLore(translatedLore);
                    }
                    item.setItemMeta(meta);
                }
            }
            group.addElement(new StaticGuiElement(character, item));
        }

        gui.addElement(group);

        // ✅ Add additional items (buttons, navigations, etc.)
        addAdditionalItems(section);
    }

    private void addAdditionalItems(Section section) {
        for (Object key : section.getKeys()) {
            if (key.equals("fish-item") || key.equals("undiscovered-fish") || key.equals("layout") || key.equals("title") || key.equals("filler") || key.equals("fish-character")) {
                continue;
            }

            Section itemSection = section.getSection(key.toString());
            if (itemSection == null) {
                continue;
            }

            char character = FishUtils.getCharFromString(itemSection.getString("character", " "), ' ');
            ItemStack item = new ItemStack(Material.valueOf(itemSection.getString("item.material", "STONE")));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(FishUtils.translateColorCodes(itemSection.getString("item.displayname", "")));
                List<String> lore = new ArrayList<>();
                for (String line : itemSection.getStringList("item.lore")) {
                    lore.add(FishUtils.translateColorCodes(line));
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            StaticGuiElement element = new StaticGuiElement(character, item, click -> {
                List<String> commands = itemSection.getStringList("click-commands");
                for (String command : commands) {
                    viewer.performCommand(command);
                }
                return true;
            });

            gui.addElement(element);
        }
    }
}
