package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.utils.NbtUtils;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BaitApplicationListener implements Listener {

    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCursor() == null)
            return;

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (clickedItem.getType() != Material.FISHING_ROD)
            return;

        if (!BaitNBTManager.isBaitObject(cursor)) {
            return;
        }


        if (!event.getWhoClicked().getGameMode().equals(GameMode.SURVIVAL)) {
            new Message(ConfigMessage.BAIT_WRONG_GAMEMODE).broadcast(event.getWhoClicked(), true, false);
            return;
        }

        ApplicationResult result;
        Bait bait = EvenMoreFish.baits.get(BaitNBTManager.getBaitName(event.getCursor()));

        ItemStack fishingRod = clickedItem;
        NbtUtils.NbtVersion nbtVersion = NbtUtils.getNbtVersion(clickedItem);
        if (nbtVersion != NbtUtils.NbtVersion.COMPAT) {
            fishingRod = convertToCompatNbtItem(nbtVersion, fishingRod);
        }

        try {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                result = BaitNBTManager.applyBaitedRodNBT(fishingRod, bait, event.getCursor().getAmount());
                EvenMoreFish.metric_baitsApplied += event.getCursor().getAmount();
            } else {
                result = BaitNBTManager.applyBaitedRodNBT(fishingRod, bait, 1);
                EvenMoreFish.metric_baitsApplied++;
            }

        } catch (MaxBaitsReachedException exception) {
            new Message(ConfigMessage.BAITS_MAXED).broadcast(event.getWhoClicked(), true, false);
            result = exception.getRecoveryResult();
        } catch (MaxBaitReachedException exception) {
            result = exception.getRecoveryResult();
            Message message = new Message(ConfigMessage.BAITS_MAXED_ON_ROD);
            message.setBaitTheme(bait.getTheme());
            message.setBait(bait.getName());
            message.broadcast(event.getWhoClicked(), true, true);
        }

        if (result == null || result.getFishingRod() == null)
            return;


        event.setCancelled(true);
        event.setCurrentItem(result.getFishingRod());

        int cursorModifier = result.getCursorItemModifier();

        if (cursor.getAmount() - cursorModifier == 0) {
            event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
        } else {
            cursor.setAmount(cursor.getAmount() + cursorModifier);
            event.getWhoClicked().setItemOnCursor(cursor);
        }
    }

    private ItemStack convertToCompatNbtItem(final NbtUtils.NbtVersion nbtVersion, final ItemStack fishingRod) {
        NBTItem nbtFishingRod = new NBTItem(fishingRod);
        final String appliedBaitString = NbtUtils.getString(nbtFishingRod, NbtUtils.Keys.EMF_APPLIED_BAIT);

        if (nbtVersion == NbtUtils.NbtVersion.LEGACY) {
            final String namespacedKey = NbtUtils.Keys.EMF_COMPOUND + ":" + NbtUtils.Keys.EMF_APPLIED_BAIT;
            nbtFishingRod.getCompound(NbtUtils.Keys.PUBLIC_BUKKIT_VALUES).removeKey(namespacedKey);

            if (Boolean.TRUE.equals(nbtFishingRod.hasKey(namespacedKey))) { //bugged version
                nbtFishingRod.removeKey(namespacedKey);
                nbtFishingRod.getCompound("display").setObject("Lore", null);
            }
        }

        if (nbtVersion == NbtUtils.NbtVersion.NBTAPI) {
            nbtFishingRod.removeKey(NbtUtils.Keys.EMF_COMPOUND + ":" + NbtUtils.Keys.EMF_APPLIED_BAIT);
        }

        NBTCompound emfCompound = nbtFishingRod.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
        emfCompound.setString(NbtUtils.Keys.EMF_APPLIED_BAIT, appliedBaitString);
        return nbtFishingRod.getItem();
    }
}
