package com.oheers.fish.selling;

import com.devskiller.friendly_id.FriendlyId;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

public class SellHelper {

    private final Inventory inventory;
    private final Player player;

    private int fishCount;

    public static void sellInventoryGui(@NotNull InventoryGui gui, @NotNull HumanEntity humanEntity) {
        if (!(humanEntity instanceof Player player)) {
            return;
        }
        gui.getElements().forEach(element -> {
            if (!(element instanceof GuiStorageElement storageElement)) {
                return;
            }
            new SellHelper(storageElement.getStorage(), player).sellFish();
        });
    }

    public SellHelper(@NotNull Inventory inventory, @NotNull Player player) {
        this.inventory = inventory;
        this.player = player;
    }

    public boolean sellFish() {
        List<SoldFish> soldFish = getTotalSoldFish();
        double totalWorth = getTotalWorth(soldFish);
        double sellPrice = Math.floor(totalWorth * 10) / 10;

        // Remove sold items
        for (ItemStack item : getPossibleSales()) {
            if (WorthNBT.getValue(item) != -1.0) {
                Fish fish = FishUtils.getFish(item);
                if (fish != null) {
                    fish.checkSellEvent();
                    fish.getSellRewards().forEach(reward -> reward.rewardPlayer(player, null));
                }
                inventory.remove(item);
            }
        }

        Economy economy = Economy.getInstance();
        if (economy.isEnabled()) {
            economy.deposit(this.player, totalWorth, true);
        }

        if (!(inventory instanceof PlayerInventory)) {
            FishUtils.giveItems(Arrays.stream(inventory.getStorageContents()).filter(Objects::nonNull).toArray(ItemStack[]::new), this.player);
        }

        // sending the sell message to the player

        AbstractMessage message = ConfigMessage.FISH_SALE.getMessage();
        if (!economy.isEnabled()) {
            message.setSellPrice("0");
        } else {
            message.setSellPrice(economy.getWorthFormat(sellPrice, true));
        }
        message.setAmount(Integer.toString(fishCount));
        message.setPlayer(this.player);
        message.send(player);

        this.player.playSound(this.player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.06f);

        if (MainConfig.getInstance().databaseEnabled()) {
            // TODO temporary fix until database PR is merged.
            try {
                logSoldFish(player.getUniqueId(), soldFish);
            } catch (Exception exception) { /* Ignored */ }
        }
        return totalWorth != 0.0;

    }

    public List<SoldFish> getTotalSoldFish() {

        List<SoldFish> soldFish = new ArrayList<>();

        for (ItemStack item : getPossibleSales()) {
            // -1.0 is given when there's no worth NBT value
            SoldFish fish = getSoldFish(item);
            if (fish != null) {
                soldFish.add(fish);
            }
        }
        return soldFish;
    }

    public List<ItemStack> getPossibleSales() {
        // Create a modifiable list from the inventory contents
        List<ItemStack> items = new ArrayList<>(Arrays.asList(inventory.getStorageContents()));

        // Remove armor from possible item list, prevents infinite money bug
        if (inventory instanceof PlayerInventory playerInventory) {
            Arrays.stream(playerInventory.getArmorContents()).filter(Objects::nonNull).forEach(items::remove);
        }

        return items;
    }

    private @Nullable SoldFish getSoldFish(final ItemStack item) {
        double itemValue = WorthNBT.getValue(item);
        if (itemValue == -1.0) {
            return null;
        }

        final String fishName = NbtUtils.getString(item, NbtKeys.EMF_FISH_NAME);
        final String fishRarity = NbtUtils.getString(item, NbtKeys.EMF_FISH_RARITY);
        Float floatLength = NbtUtils.getFloat(item, NbtKeys.EMF_FISH_LENGTH);
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
        this.fishCount = count;

        return Math.floor(totalValue * 10) / 10;
    }

    private void logSoldFish(final UUID uuid, @NotNull List<SoldFish> soldFish) {
        int userId = EvenMoreFish.getInstance().getDatabase().getUserId(uuid);
        final String transactionId = FriendlyId.createFriendlyId();
        final Timestamp timestamp = Timestamp.from(Instant.now());

        EvenMoreFish.getInstance().getDatabase().createTransaction(transactionId, userId, timestamp);
        for(final SoldFish fish: soldFish) {
            EvenMoreFish.getInstance().getDatabase().createSale(transactionId, fish.getName(),fish.getRarity(), fish.getAmount(),fish.getLength(), fish.getTotalValue());
        }

        double moneyEarned = getTotalWorth(soldFish);
        int fishSold = calcFishSold(soldFish);
        DataManager.getInstance().getUserReportIfExists(uuid).incrementFishSold(fishSold);
        DataManager.getInstance().getUserReportIfExists(uuid).incrementMoneyEarned(moneyEarned);
    }

    private int calcFishSold(@NotNull List<SoldFish> soldFish) {
        return soldFish.stream().mapToInt(SoldFish::getAmount).sum();
    }

    public double getTotalWorth() {
        return getTotalWorth(getTotalSoldFish());
    }

}
