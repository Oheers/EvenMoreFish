package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.config.BaitFile;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BaitNBTManager {

    private BaitNBTManager() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks whether the item has nbt to suggest it is a bait object.
     *
     * @param itemStack The item stack that could potentially be a bait.
     * @return If the item stack is a bait or not (or if itemStack is null)
     */
    public static boolean isBaitObject(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (itemStack.hasItemMeta()) {
            return NbtUtils.hasKey(itemStack, NbtKeys.EMF_BAIT);
        } else {
            return false;
        }
    }

    /**
     * @param itemStack The item stack that is a bait.
     * @return The name of the bait.
     */
    public static @Nullable String getBaitName(@NotNull ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            return NbtUtils.getString(itemStack, NbtKeys.EMF_BAIT);
        }
        return null;
    }

    /**
     * Gives an ItemStack the nbt required for the plugin to see it as a valid bait that can be applied to fishing rods.
     * It is inadvisable to use a block as a bait, as these will lose their nbt tags if they're placed - and the plugin
     * will forget that it was ever a bait.
     *
     * @param item The item stack being turned into a bait.
     * @param bait The name of the bait to be applied.
     */
    public static ItemStack applyBaitNBT(ItemStack item, String bait) {
        if (item == null) {
            return null;
        }
        NBT.modify(item, nbt -> {
            nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND).setString(NbtKeys.EMF_BAIT, bait);
        });
        return item;
    }

    /**
     * This checks against the item's NBTs to work out whether the fishing rod passed through has applied baits.
     *
     * @param itemStack The fishing rod that could maybe have bait NBTs applied.
     * @return Whether the fishing rod has bait NBT.
     */
    public static boolean isBaitedRod(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        if (itemStack.getType() != Material.FISHING_ROD) {
            return false;
        }

        if (itemStack.hasItemMeta()) {
            return NbtUtils.hasKey(itemStack, NbtKeys.EMF_APPLIED_BAIT);
        }

        return false;
    }

    /**
     * This applies a bait NBT reference to a fishing rod, and also checks whether the bait is already applied,
     * making an effort to increase it rather than apply it.
     *
     * @param item     The fishing rod having its bait applied.
     * @param bait     The name of the bait being applied.
     * @param quantity The number of baits being applied. These must be of the same bait.
     * @return An ApplicationResult containing the updated "item" itemstack and the remaining baits for the cursor.
     * @throws MaxBaitsReachedException When too many baits are tried to be applied to a fishing rod.
     * @throws MaxBaitReachedException  When one of the baits has hit maximum set by max-baits in baits.yml
     */
    public static ApplicationResult applyBaitedRodNBT(ItemStack item, Bait bait, int quantity) throws MaxBaitsReachedException, MaxBaitReachedException {
        boolean doingLoreStuff = BaitFile.getInstance().doRodLore();
        AtomicBoolean maxBait = new AtomicBoolean(false);
        AtomicInteger cursorModifier = new AtomicInteger();

        StringBuilder combined = new StringBuilder();
        if (isBaitedRod(item)) {
            try {
                if (doingLoreStuff) {
                    FishUtils.editMeta(item, meta -> meta.setLore(deleteOldLore(item)));
                }
            } catch (IndexOutOfBoundsException exception) {
                EvenMoreFish.getInstance()
                        .getLogger()
                        .severe("Failed to apply bait: " + bait.getName() + " to a user's fishing rod. This is likely caused by a change in format in the baits.yml config.");
                return null;
            }

            String[] baitList = NbtUtils.getBaitArray(item);

            boolean foundBait = false;

            for (String baitName : baitList) {
                if (baitName.split(":")[0].equals(bait.getName())) {
                    if (bait.isInfinite()) {
                        combined.append(baitName.split(":")[0]).append(":∞,");
                    } else {
                        int newQuantity = Integer.parseInt(baitName.split(":")[1]) + quantity;

                        if (newQuantity > bait.getMaxApplications() && bait.getMaxApplications() != -1) {
                            combined.append(baitName.split(":")[0]).append(":").append(bait.getMaxApplications()).append(",");
                            cursorModifier.set(-bait.getMaxApplications() + (newQuantity - quantity));
                            maxBait.set(true);
                        } else if (newQuantity != 0) {
                            combined.append(baitName.split(":")[0]).append(":").append(newQuantity).append(",");
                            cursorModifier.set(-quantity);
                        }
                    }
                    foundBait = true;
                } else {
                    combined.append(baitName).append(",");
                }
            }

            // We can manage the last character not being a colon if we have to add it in ourselves.
            if (!foundBait) {

                if (getNumBaitsApplied(item) >= BaitFile.getInstance().getMaxBaits()) {
                    // the lore's been taken out, we're not going to be doing anymore here, so we're just re-adding it now.
                    if (doingLoreStuff) {
                        FishUtils.editMeta(item, meta -> meta.setLore(newApplyLore(item)));
                    }
                    throw new MaxBaitsReachedException("Max baits reached.", new ApplicationResult(item, cursorModifier.get()));
                }

                if (quantity > bait.getMaxApplications() && bait.getMaxApplications() != -1) {
                    cursorModifier.set(-bait.getMaxApplications());
                    combined.append(bait.getName()).append(":").append(bait.getMaxApplications());
                    maxBait.set(true);
                } else {
                    combined.append(bait.getName()).append(":").append(quantity);
                    cursorModifier.set(-quantity);
                }
            } else {
                if (!combined.isEmpty()) {
                    combined.deleteCharAt(combined.length() - 1);
                }
            }
            NBT.modify(item, nbt -> {
                ReadWriteNBT emfCompound = nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND);
                if (!combined.isEmpty()) {
                    emfCompound.setString(NbtKeys.EMF_APPLIED_BAIT, combined.toString());
                } else {
                    emfCompound.removeKey(NbtKeys.EMF_APPLIED_BAIT);
                }
            });
        } else {
            NBT.modify(item, nbt -> {
                ReadWriteNBT compound = nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND);
                if (quantity > bait.getMaxApplications() && bait.getMaxApplications() != -1) {
                    combined.append(bait.getName()).append(":").append(bait.getMaxApplications());
                    compound.setString(NbtKeys.EMF_APPLIED_BAIT, combined.toString());
                    cursorModifier.set(-bait.getMaxApplications());
                    maxBait.set(true);
                } else {
                    combined.append(bait.getName()).append(":").append(quantity);
                    compound.setString(NbtKeys.EMF_APPLIED_BAIT, combined.toString());
                    cursorModifier.set(-quantity);
                }
            });
        }

        if (doingLoreStuff && !combined.isEmpty()) {
            FishUtils.editMeta(item, meta -> meta.setLore(newApplyLore(item)));
        }

        if (maxBait.get()) {
            throw new MaxBaitReachedException(bait, new ApplicationResult(item, cursorModifier.get()));
        }

        return new ApplicationResult(item, cursorModifier.get());
    }

    /**
     * This fetches a random bait applied to the rod, based on the application-weight of the baits (if they exist). The
     * weight defaults to "1" if there is no value applied for them.
     *
     * @param fishingRod The fishing rod.
     * @return A random bait applied to the fishing rod.
     */
    public static @Nullable Bait randomBaitApplication(ItemStack fishingRod) {
        if (fishingRod == null || fishingRod.getItemMeta() == null) {
            return null;
        }

        String[] baitNameList = NbtUtils.getBaitArray(fishingRod);
        List<Bait> baitList = new ArrayList<>();

        for (String baitName : baitNameList) {

            Bait bait = BaitManager.getInstance().getBait(baitName.split(":")[0]);
            if (bait != null) {
                baitList.add(bait);
            }

        }

        // Fix IndexOutOfBoundsException caused by the list being empty.
        if (baitList.isEmpty()) {
            return null;
        }

        double totalWeight = baitList.stream().mapToDouble(Bait::getApplicationWeight).sum();

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < baitList.size() - 1; ++idx) {
            r -= baitList.get(idx).getApplicationWeight();
            if (r <= 0.0) {
                break;
            }
        }
        return baitList.get(idx);
    }

    /**
     * Calculates a random bait to throw out based on their catch-weight. It uses the same weight algorithm as
     * randomBaitApplication, using the baits from the main class in the baits list.
     *
     * @return A random bait weighted by its catch-weight.
     */
    public static @Nullable Bait randomBaitCatch() {

        Map<String, Bait> baitMap = BaitManager.getInstance().getBaitMap();
        List<Bait> baitList = new ArrayList<>(baitMap.values());

        // Fix IndexOutOfBoundsException caused by the list being empty.
        if (baitList.isEmpty()) {
            return null;
        }

        double totalWeight = baitList.stream().mapToDouble(Bait::getCatchWeight).sum();

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < baitList.size() - 1; ++idx) {
            r -= baitList.get(idx).getCatchWeight();
            if (r <= 0.0) {
                break;
            }
        }

        return baitList.get(idx);
    }

    /**
     * Runs through the metadata of the rod to try and figure out whether a certain bait is applied or not.
     *
     * @param itemStack The fishing rod in item stack form.
     * @param bait      The name of the bait that could have been applied, must be the same as the time it was applied to the rod.
     * @return If the fishing rod contains the bait or not.
     */
    public static boolean hasBaitApplied(ItemStack itemStack, String bait) {
        if (itemStack == null) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return false;
        }

        String[] baitList = NbtUtils.getBaitArray(itemStack);

        for (String appliedBait : baitList) {
            if (appliedBait.split(":")[0].equals(bait)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes all the baits stored in the nbt of the fishing rod. It then returns the total number of baits deleted just
     * incase you fancy doing something special with this number. It first checks whether there's any baits actually on
     * the rod in the first place. It loops through each bait stored to find out how many will be deleted then simply removes
     * the namespacedkey from the fishing rod.
     *
     * @param itemStack The fishing rod with baits on it/
     * @return The number of baits that were deleted in total.
     */
    public static int deleteAllBaits(ItemStack itemStack) {
        if (!NbtUtils.hasKey(itemStack, NbtKeys.EMF_APPLIED_BAIT)) {
            return 0;
        }

        int totalDeleted = 0;
        String[] baitList = NbtUtils.getBaitArray(itemStack);
        for (String appliedBait : baitList) {
            String quantityStr = appliedBait.split(":")[1];
            if (!quantityStr.equals("∞")) {
                totalDeleted += Integer.parseInt(quantityStr);
            } else {
                totalDeleted += 1; // Count infinite baits as 1
            }
        }
        NBT.modify(itemStack, nbt -> {
            nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND).removeKey(NbtKeys.EMF_APPLIED_BAIT);
        });

        return totalDeleted;
    }

    public static List<String> newApplyLore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return Collections.emptyList();
        }

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        List<String> format = BaitFile.getInstance().getRodLoreFormat();
        for (String lineAddition : format) {
            if (lineAddition.equals("{baits}")) {
                String rodNBT = NbtUtils.getString(itemStack, NbtKeys.EMF_APPLIED_BAIT);

                if (rodNBT == null || rodNBT.isEmpty()) {
                    return lore;
                }

                int baitCount = 0;

                for (String bait : rodNBT.split(",")) {
                    baitCount++;
                    AbstractMessage message = EvenMoreFish.getAdapter().createMessage(BaitFile.getInstance().getBaitFormat());
                    // TODO this is to prevent an ArrayIndexOutOfBoundsException, but it should be handled in a better way.
                    try {
                        message.setAmount(bait.split(":")[1]);
                    } catch (ArrayIndexOutOfBoundsException exception) {
                        message.setAmount("N/A");
                    }
                    message.setBait(getBaitFormatted(bait.split(":")[0]));
                    lore.add(message.getLegacyMessage());
                }

                if (BaitFile.getInstance().showUnusedBaitSlots()) {
                    for (int i = baitCount; i < BaitFile.getInstance().getMaxBaits(); i++) {
                        lore.add(FishUtils.translateColorCodes(BaitFile.getInstance().unusedBaitSlotFormat()));
                    }
                }
            } else {
                AbstractMessage message = EvenMoreFish.getAdapter().createMessage(lineAddition);
                message.setCurrentBaits(Integer.toString(getNumBaitsApplied(itemStack)));
                message.setMaxBaits(Integer.toString(BaitFile.getInstance().getMaxBaits()));
                lore.add(message.getLegacyMessage());
            }
        }

        return lore;
    }

    /**
     * This deletes all the old lore inserted by the plugin to the baited fishing rod. If the config value for the lore
     * format had lines added/removed this will break the old rods.
     *
     * @param itemStack The lore of the itemstack having the bait section of its lore removed.
     * @throws IndexOutOfBoundsException When the fishing rod doesn't have enough lines of lore to delete, this could be
     *                                   caused by a modification to the format in the baits.yml config.
     */
    public static List<String> deleteOldLore(ItemStack itemStack) throws IndexOutOfBoundsException {
        if (!itemStack.hasItemMeta() || itemStack.getItemMeta() == null || !itemStack.getItemMeta().hasLore()) {
            return Collections.emptyList();
        }

        List<String> lore = itemStack.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) {
            return Collections.emptyList();
        }

        if (BaitFile.getInstance().showUnusedBaitSlots()) {
            // starting at 1, because at least one bait replacing {baits} is repeated.
            int maxBaits = BaitFile.getInstance().getMaxBaits() + BaitFile.getInstance().getRodLoreFormat().size();
            //todo, to help this be compliant with java:S5413, we should iterate in reverse order, this should be done in another pr, left here for reference
            //compliant version
            for (int i = 1; i < maxBaits; i++) {
                lore.remove(lore.size() - 1);
            }
        } else {
            // starting at 1, because at least one bait replacing {baits} is repeated.
            int numBaitsApplied = getNumBaitsApplied(itemStack) + BaitFile.getInstance().getRodLoreFormat().size();
            //compliant version
            for (int i = 1; i < numBaitsApplied; i++) {
                lore.remove(lore.size() - 1);
            }
        }

        return lore;
    }

    /**
     * Works out how many baits are applied to an object based on the nbt data.
     *
     * @param itemStack The fishing rod with baits applied
     * @return How many baits have been applied to this fishing rod.
     */
    private static int getNumBaitsApplied(ItemStack itemStack) {
        String rodNBT = NbtUtils.getString(itemStack, NbtKeys.EMF_APPLIED_BAIT);
        if (rodNBT == null) {
            return 1;
        }

        return rodNBT.split(",").length;
    }

    /**
     * Checks the bait from baitID to see if it has a displayname and returns that if necessary - else it just returns
     * the baitID itself.
     *
     * @param baitID The baitID the bait is registered under in baits.yml
     * @return How the bait should look in the lore of the fishing rod, for example.
     */
    private static String getBaitFormatted(String baitID) {
        Bait bait = BaitManager.getInstance().getBait(baitID);
        if (bait == null) {
            EvenMoreFish.getInstance().getLogger().warning("Bait " + baitID + " is not a valid bait!");
            return "Invalid Bait";
        }
        return FishUtils.translateColorCodes(bait.getDisplayName());
    }

}