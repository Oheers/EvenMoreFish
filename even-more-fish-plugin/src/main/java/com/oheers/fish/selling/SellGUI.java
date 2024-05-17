package com.oheers.fish.selling;

import com.devskiller.friendly_id.FriendlyId;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.oheers.fish.Economy;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.NbtUtils;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.utils.ItemUtils;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;

public class SellGUI extends InventoryGui {

    private final Player player;
    private final Inventory fishInventory;
    private SellState sellState = SellState.NORMAL;
    private double value;
    private int fishCount;
    private MyScheduledTask task;

    public SellGUI(Player player) {
        super(EvenMoreFish.getInstance(), new Message(ConfigMessage.WORTH_GUI_NAME).getRawMessage(true), MainConfig.getInstance().getSellGUILayout());
        this.player = player;
        this.fishInventory = Bukkit.createInventory(null, 54);
        // GUI Filler Element
        addElement(getGUIFiller());
        // Sell Item Element
        addElement(getSellItem());
        // Sell All Item Element
        addElement(getSellAllItem());
        setCloseAction(close -> {
            task.cancel();
            task = null;
            if (MainConfig.getInstance().sellOverDrop()) {
                sell(false);
            }
            doRescue();
            return false;
        });
        addElement(new GuiStorageElement('i', fishInventory));
        task = EvenMoreFish.getScheduler().runTaskTimer(this::draw, 5L, 5L);
    }

    public void open() {
        show(player);
    }

    private void setSellState(SellState state) {
        this.sellState = state;
    }

    public Player getPlayer() {
        return player;
    }

    public DynamicGuiElement getGUIFiller() {
        // the gray glass panes at the bottom
        ItemStack fill = new ItemStack(ItemUtils.getMaterial(MainConfig.getInstance().getFiller(), Material.GRAY_STAINED_GLASS_PANE));
        ItemStack error = new ItemStack(ItemUtils.getMaterial(MainConfig.getInstance().getFillerError(), Material.RED_STAINED_GLASS_PANE));
        ItemMeta fillMeta = fill.getItemMeta();
        ItemMeta errMeta = error.getItemMeta();

        if (fillMeta != null) {
            fillMeta.setDisplayName(ChatColor.RESET + "");
            fill.setItemMeta(fillMeta);
        }

        if (errMeta != null) {
            errMeta.setDisplayName(ChatColor.RESET + "");
            error.setItemMeta(errMeta);
        }

        return new DynamicGuiElement('f', (viewer) -> {
            if (isError()) {
                return new StaticGuiElement('f', WorthNBT.attributeDefault(error));
            } else {
                return new StaticGuiElement('f', WorthNBT.attributeDefault(fill));
            }
        });

    }

    public boolean isError() {
        return sellState.equals(SellState.ERROR);
    }


    public DynamicGuiElement getSellAllItem() {
        // Dynamic since this can change.
        return new DynamicGuiElement('a', (viewer) -> getSellAllItemFromState());
    }

    public StaticGuiElement getSellAllItemFromState() {
        switch (sellState) {
            case NORMAL:
                ItemStack snIcon = new ItemStack(MainConfig.getInstance().getSellAllMaterial());
                ItemMeta sellNormalMeta = snIcon.getItemMeta();
                if (sellNormalMeta != null) {
                    // Display
                    sellNormalMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_SELL_ALL_BUTTON_NAME).getRawMessage(false));

                    // Lore
                    Message message = new Message(ConfigMessage.WORTH_GUI_SELL_ALL_BUTTON_LORE);
                    message.setSellPrice(String.valueOf(formatWorth(getTotalWorth(true))));
                    sellNormalMeta.setLore(new ArrayList<>(Arrays.asList(message.getRawMessage(true).split("\n"))));

                    snIcon.setItemMeta(sellNormalMeta);
                }
                ItemUtils.glowify(snIcon);
                return new StaticGuiElement('a', WorthNBT.attributeDefault(snIcon), click -> {
                    if (getTotalSoldFish(true).isEmpty()) {
                        setSellState(SellState.ERROR);
                    } else {
                        setSellState(SellState.CONFIRM);
                    }
                    draw();
                    return true;
                });

            case CONFIRM:
                ItemStack scIcon = new ItemStack(MainConfig.getInstance().getSellAllConfirmMaterial());
                ItemMeta sellConfirmMeta = scIcon.getItemMeta();
                if (sellConfirmMeta != null) {
                    // Display
                    sellConfirmMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_CONFIRM_ALL_BUTTON_NAME).getRawMessage(false));

                    // Lore
                    Message message = new Message(ConfigMessage.WORTH_GUI_SELL_ALL_BUTTON_LORE);
                    message.setSellPrice(String.valueOf(formatWorth(getTotalWorth(false))));
                    sellConfirmMeta.setLore(new ArrayList<>(Arrays.asList(message.getRawMessage(true).split("\n"))));

                    scIcon.setItemMeta(sellConfirmMeta);
                }
                ItemUtils.glowify(scIcon);

                return new StaticGuiElement('a', WorthNBT.attributeDefault(scIcon), click -> {
                    sell(true);
                    close();
                    return true;
                });

            case ERROR:
                ItemStack seIcon = new ItemStack(MainConfig.getInstance().getSellAllErrorMaterial());
                ItemMeta sellErrorMeta = seIcon.getItemMeta();
                if (sellErrorMeta != null) {
                    // Display
                    sellErrorMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_NO_VAL_ALL_BUTTON_NAME).getRawMessage(false));

                    // Lore
                    Message message = new Message(ConfigMessage.WORTH_GUI_SELL_BUTTON_LORE);
                    message.setSellPrice(String.valueOf(formatWorth(getTotalWorth(false))));
                    sellErrorMeta.setLore(new ArrayList<>(Arrays.asList(message.getRawMessage(true).split("\n"))));

                    seIcon.setItemMeta(sellErrorMeta);
                }
                ItemUtils.glowify(seIcon);

                return new StaticGuiElement('a', WorthNBT.attributeDefault(seIcon), click -> {
                    close();
                    return true;
                });
            // This should NEVER happen, but the code won't compile without it.
            default:
                return null;
        }
    }

    public DynamicGuiElement getSellItem() {
        // Dynamic since this can change.
        return new DynamicGuiElement('s', (viewer) -> getSellItemFromState());
    }

    private StaticGuiElement getSellItemFromState() {
        switch (sellState) {
            case NORMAL:
                ItemStack snIcon = new ItemStack(Material.valueOf(MainConfig.getInstance().getSellItem()));
                ItemMeta sellNormalMeta = snIcon.getItemMeta();
                if (sellNormalMeta != null) {
                    // Display
                    sellNormalMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_SELL_BUTTON_NAME).getRawMessage(false));

                    // Lore
                    Message message = new Message(ConfigMessage.WORTH_GUI_SELL_LORE);
                    message.setSellPrice(String.valueOf(formatWorth(getTotalWorth(false))));
                    sellNormalMeta.setLore(new ArrayList<>(Arrays.asList(message.getRawMessage(true).split("\n"))));

                    snIcon.setItemMeta(sellNormalMeta);
                }
                ItemUtils.glowify(snIcon);
                return new StaticGuiElement('s', WorthNBT.attributeDefault(snIcon), click -> {
                    if (click.getType().isRightClick()) {
                        close();
                        return true;
                    }
                    if (getTotalSoldFish(false).isEmpty()) {
                        setSellState(SellState.ERROR);
                    } else {
                        setSellState(SellState.CONFIRM);
                    }
                    draw();
                    return true;
                });

            case CONFIRM:
                ItemStack scIcon = new ItemStack(Material.valueOf(MainConfig.getInstance().getSellItemConfirm()));
                ItemMeta sellConfirmMeta = scIcon.getItemMeta();
                if (sellConfirmMeta != null) {
                    // Display
                    sellConfirmMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_SELL_BUTTON_NAME).getRawMessage(false));

                    // Lore
                    Message message = new Message(ConfigMessage.WORTH_GUI_SELL_LORE);
                    message.setSellPrice(String.valueOf(formatWorth(getTotalWorth(false))));
                    sellConfirmMeta.setLore(new ArrayList<>(Arrays.asList(message.getRawMessage(true).split("\n"))));

                    scIcon.setItemMeta(sellConfirmMeta);
                }
                ItemUtils.glowify(scIcon);

                return new StaticGuiElement('s', WorthNBT.attributeDefault(scIcon), click -> {
                    if (click.getType().isRightClick()) {
                        close();
                        return true;
                    }
                    sell(false);
                    close();
                    return true;
                });

            case ERROR:
                ItemStack seIcon = new ItemStack(Material.valueOf(MainConfig.getInstance().getSellItemError()));
                ItemMeta sellErrorMeta = seIcon.getItemMeta();
                if (sellErrorMeta != null) {
                    // Display
                    sellErrorMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_NO_VAL_BUTTON_NAME).getRawMessage(false));

                    // Lore
                    Message message = new Message(ConfigMessage.WORTH_GUI_NO_VAL_BUTTON_LORE);
                    message.setSellPrice(String.valueOf(formatWorth(getTotalWorth(false))));
                    sellErrorMeta.setLore(new ArrayList<>(Arrays.asList(message.getRawMessage(true).split("\n"))));

                    seIcon.setItemMeta(sellErrorMeta);
                }
                ItemUtils.glowify(seIcon);

                return new StaticGuiElement('s', WorthNBT.attributeDefault(seIcon), click -> {
                    close();
                    return true;
                });
            // This should NEVER happen, but the code won't compile without it.
            default:
                return null;
        }
    }

    public List<SoldFish> getTotalSoldFish(boolean inventory) {

        if (this.fishInventory == null) {
            return Collections.emptyList();
        }
        
        List<SoldFish> soldFish = new ArrayList<>();
        
        if (inventory) {
            for (ItemStack item : player.getInventory().getStorageContents()) {
                // -1.0 is given when there's no worth NBT value
                SoldFish fish = getSoldFish(item);
                if (fish != null) {
                    soldFish.add(fish);
                }
            }
        } else {
            for (ItemStack item : this.fishInventory.getContents()) {
                // -1.0 is given when there's no worth NBT value
                SoldFish fish = getSoldFish(item);
                if (fish != null) {
                    soldFish.add(fish);
                }
            }
        }
        return soldFish;
    }
    
    private @Nullable SoldFish getSoldFish(final ItemStack item) {
        double itemValue = WorthNBT.getValue(item);
        if (itemValue == -1.0) {
            return null;
        }
        
        NBTItem nbtItem = new NBTItem(item);
        final String fishName = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_NAME);
        final String fishRarity = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_FISH_RARITY);
        Float floatLength = NbtUtils.getFloat(nbtItem, NbtUtils.Keys.EMF_FISH_LENGTH);
        final double fishLength = floatLength == null ? -1.0 : floatLength;
        final double fishValue = WorthNBT.getValue(item);
        
        return new SoldFish(fishName, fishRarity, item.getAmount(), fishValue * item.getAmount(), fishLength);
    }
    
    
    public double getTotalWorth(final List<SoldFish> soldFish) {
        double totalValue = 0.0d;
        int count = 0;
        for(SoldFish sold: soldFish) {
            totalValue += sold.getTotalValue();
            count += sold.getAmount();
        }
        this.value = totalValue;
        this.fishCount = count;

        // Run this through the Economy#prepareValue method so the value is correct
        // double for Vault, int for PlayerPoints, 0 when there is no economy plugin
        return Economy.prepareValue(Math.floor(totalValue * 10) / 10);
    }

    public double getTotalWorth(boolean inventory) {
        return getTotalWorth(getTotalSoldFish(inventory));
    }

    public String formatWorth(double totalWorth) {
        switch (EvenMoreFish.getInstance().getEconomy().getEconomyType()) {
            case GRIEF_PREVENTION:
                if ((int) totalWorth == 1) {
                    return (int) totalWorth + " Claim Block";
                } else {
                    return (int) totalWorth + " Claim Blocks";
                }
            case PLAYER_POINTS:
                if ((int) totalWorth == 1) {
                    return (int) totalWorth + " Player Point";
                } else {
                    return (int) totalWorth + " Player Points";
                }
            case VAULT:
                DecimalFormat format = new DecimalFormat(new Message(ConfigMessage.SELL_PRICE_FORMAT).getRawMessage(false));
                return format.format(totalWorth);
            // Includes NONE type
            default:
                return String.valueOf(totalWorth);
        }
    }

    // will drop only non-fish items if the method is called from selling, and everything if it's just a gui close
    public void close() {
        player.closeInventory();
    }

    // for each item in the menu, if it isn't a default menu item, it's dropped at the player's feet
    public void doRescue() {
        List<ItemStack> throwing = new ArrayList<>();
        for (ItemStack i : this.fishInventory.getContents()) {
            if (i != null) {
                if (!WorthNBT.isDefault(i)) {
                    throwing.add(i);
                }
            }
        }
        FishUtils.giveItems(throwing, this.player);
    }

    public boolean sell(boolean sellAll) {
        List<SoldFish> soldFish = getTotalSoldFish(sellAll);
        double totalWorth = getTotalWorth(soldFish);
        double sellPrice = Math.floor(totalWorth * 10) / 10;

        Economy economy = EvenMoreFish.getInstance().getEconomy();
        if (economy != null && economy.isEnabled()) {
            economy.deposit(this.player, totalWorth);
        }

        // sending the sell message to the player

        Message message = new Message(ConfigMessage.FISH_SALE);
        message.setSellPrice(formatWorth(sellPrice));
        message.setAmount(Integer.toString(fishCount));
        message.setPlayer(this.player.toString());
        message.broadcast(player, true);

        this.player.playSound(this.player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.06f);

        if (sellAll) {
            for (ItemStack item : this.player.getInventory()) {
                if (FishUtils.isFish(item)) {
                    Fish fish = FishUtils.getFish(item);
                    if (fish != null) {
                        fish.checkSellEvent();
                        fish.getSellRewards().forEach(reward -> reward.rewardPlayer(player, null));
                    }
                    this.player.getInventory().remove(item);
                }
            }
        } else {
            // Remove sold items
            for (ItemStack item : fishInventory.getContents()) {
                if (WorthNBT.getValue(item) != -1.0) {
                    Fish fish = FishUtils.getFish(item);
                    if (fish != null) {
                        fish.checkSellEvent();
                        fish.getSellRewards().forEach(reward -> reward.rewardPlayer(player, null));
                    }
                    fishInventory.remove(item);
                }
            }
        }
        if (MainConfig.getInstance().databaseEnabled()) logSoldFish(player.getUniqueId(),soldFish);
        return totalWorth != 0.0;
    }
    
    private void logSoldFish(final UUID uuid, @NotNull List<SoldFish> soldFish) {
        int userId = EvenMoreFish.getInstance().getDatabaseV3().getUserID(uuid);
        final String transactionId = FriendlyId.createFriendlyId();
        final Timestamp timestamp = Timestamp.from(Instant.now());

        EvenMoreFish.getInstance().getDatabaseV3().createTransaction(transactionId, userId, timestamp);
        for(final SoldFish fish: soldFish) {
            EvenMoreFish.getInstance().getDatabaseV3().createSale(transactionId, timestamp, userId, fish.getName(),fish.getRarity(), fish.getAmount(),fish.getLength(), fish.getTotalValue());
        }
        
        double moneyEarned = getTotalWorth(soldFish);
        int fishSold = calcFishSold(soldFish);
        DataManager.getInstance().getUserReportIfExists(uuid).incrementFishSold(fishSold);
        DataManager.getInstance().getUserReportIfExists(uuid).incrementMoneyEarned(moneyEarned);
    }
    
    private int calcFishSold(@NotNull List<SoldFish> soldFish) {
        return soldFish.stream().mapToInt(SoldFish::getAmount).sum();
    }

    private enum SellState {
        NORMAL,
        CONFIRM,
        ERROR
    }

}
