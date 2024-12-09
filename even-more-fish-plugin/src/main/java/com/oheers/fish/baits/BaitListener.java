package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import com.oheers.fish.utils.nbt.NbtVersion;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class BaitListener implements Listener {

    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCursor() == null) {
            return;
        }

        if (MainConfig.getInstance().shouldProtectBaitedRods() && anvilCheck(event)) {
            return;
        }

        ItemStack potentialFishingRod = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (potentialFishingRod.getType() != Material.FISHING_ROD)
            return;

        if (!BaitNBTManager.isBaitObject(cursor)) {
            return;
        }

        GameMode gameMode = event.getWhoClicked().getGameMode();

        if (!gameMode.equals(GameMode.SURVIVAL) && !gameMode.equals(GameMode.ADVENTURE)) {
            ConfigMessage.BAIT_WRONG_GAMEMODE.getMessage().send(event.getWhoClicked());
            return;
        }

        ApplicationResult result;
        Bait bait = BaitManager.getInstance().getBait(BaitNBTManager.getBaitName(event.getCursor()));

        if (bait == null) {
            return;
        }

        NbtVersion nbtVersion = NbtVersion.getVersion(potentialFishingRod);
        if (nbtVersion != NbtVersion.COMPAT) {
            convertToCompatNbtItem(nbtVersion, potentialFishingRod);
        }

        try {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                result = BaitNBTManager.applyBaitedRodNBT(potentialFishingRod, bait, event.getCursor().getAmount());
                EvenMoreFish.getInstance().incrementMetricBaitsApplied(event.getCursor().getAmount());
            } else {
                result = BaitNBTManager.applyBaitedRodNBT(potentialFishingRod, bait, 1);
                EvenMoreFish.getInstance().incrementMetricBaitsApplied(1);
            }

        } catch (MaxBaitsReachedException exception) {
            ConfigMessage.BAITS_MAXED.getMessage().send(event.getWhoClicked());
            result = exception.getRecoveryResult();
        } catch (MaxBaitReachedException exception) {
            result = exception.getRecoveryResult();
            AbstractMessage message = ConfigMessage.BAITS_MAXED_ON_ROD.getMessage();
            message.setBaitTheme(bait.getTheme());
            message.setBait(bait.getName());
            message.send(event.getWhoClicked());
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

    private void convertToCompatNbtItem(final NbtVersion nbtVersion, final ItemStack fishingRod) {
        final String appliedBaitString = NbtUtils.getString(fishingRod, NbtKeys.EMF_APPLIED_BAIT);

        if (nbtVersion == NbtVersion.LEGACY) {
            final String namespacedKey = NbtKeys.EMF_COMPOUND + ":" + NbtKeys.EMF_APPLIED_BAIT;
            NBT.modify(fishingRod,nbt -> {
                nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES).removeKey(namespacedKey);
            });

            if (NBT.get(fishingRod, nbt -> {
                return nbt.hasTag(namespacedKey);
            })) {
                NBT.modify(fishingRod, nbt -> {
                    nbt.removeKey(namespacedKey);
                    nbt.getCompound("display").getStringList("Lore").clear();
                });
            }
        }

        if (nbtVersion == NbtVersion.NBTAPI) {
            NBT.modify(fishingRod, nbt -> {
                nbt.removeKey(NbtKeys.EMF_COMPOUND + ":" + NbtKeys.EMF_APPLIED_BAIT);
            });
        }

        NBT.modify(fishingRod, nbt -> {
            nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND).setString(NbtKeys.EMF_APPLIED_BAIT, appliedBaitString);
        });

    }

    private boolean anvilCheck(InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof AnvilInventory inv) || !(event.getWhoClicked() instanceof Player player)) {
            return false;
        }
        if (event.getSlot() == 2 && BaitNBTManager.isBaitedRod(inv.getItem(1))) {
            event.setCancelled(true);
            player.closeInventory();
            ConfigMessage.BAIT_ROD_PROTECTION.getMessage().send(player);
            return true;
        }
        return false;
    }

}
