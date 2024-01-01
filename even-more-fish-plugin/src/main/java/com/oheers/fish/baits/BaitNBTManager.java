package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.utils.FishUtils;
import com.oheers.fish.utils.NbtUtils;
import com.oheers.fish.config.messages.OldMessage;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class BaitNBTManager {

    /**
     * Checks whether the item has nbt to suggest it is a bait object.
     *
     * @param itemStack The item stack that could potentially be a bait.
     * @return If the item stack is a bait or not (or if itemStack is null)
     */
    public static boolean isBaitObject(ItemStack itemStack) {
        if (itemStack == null) return false;

        if (itemStack.hasItemMeta()) {
            return NbtUtils.hasKey(new NBTItem(itemStack), NbtUtils.Keys.EMF_BAIT);
        } else return false;
    }

    /**
     * @param itemStack The item stack that is a bait.
     * @return The name of the bait.
     */
    public static @Nullable String getBaitName(@NotNull ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            return NbtUtils.getString(new NBTItem(itemStack), NbtUtils.Keys.EMF_BAIT);
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
        if (item == null) return null;
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound emfCompound = nbtItem.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
        emfCompound.setString(NbtUtils.Keys.EMF_BAIT, bait);
        return nbtItem.getItem();
    }

    /**
     * This checks against the item's NBTs to work out whether the fishing rod passed through has applied baits.
     *
     * @param itemStack The fishing rod that could maybe have bait NBTs applied.
     * @return Whether the fishing rod has bait NBT.
     */
    public static boolean isBaitedRod(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (itemStack.getType() != Material.FISHING_ROD) return false;

        if (itemStack.hasItemMeta()) {
            return NbtUtils.hasKey(new NBTItem(itemStack), NbtUtils.Keys.EMF_APPLIED_BAIT);
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
        boolean doingLoreStuff = EvenMoreFish.baitFile.doRodLore();
        boolean maxBait = false;
        int cursorModifier = 0;

        StringBuilder combined = new StringBuilder();

        NBTItem nbtItem;
        if (isBaitedRod(item)) {

            try {
                if (doingLoreStuff) {
                    ItemMeta meta = item.getItemMeta();
                    meta.setLore(deleteOldLore(item));
                    item.setItemMeta(meta);
                }
            } catch (IndexOutOfBoundsException exception) {
                EvenMoreFish.logger.log(Level.SEVERE, "Failed to apply bait: " + bait.getName() + " to a user's fishing rod. This is likely caused by a change in format in the baits.yml config.");
                return null;
            }

            nbtItem = new NBTItem(item);
            String[] baitList = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_APPLIED_BAIT).split(",");

            boolean foundBait = false;

            for (String baitName : baitList) {
                if (baitName.split(":")[0].equals(bait.getName())) {
                    int newQuantity = Integer.parseInt(baitName.split(":")[1]) + quantity;

                    if (newQuantity > bait.getMaxApplications() && bait.getMaxApplications() != -1) {
                        combined.append(baitName.split(":")[0]).append(":").append(bait.getMaxApplications()).append(",");
                        // new cursor amt = -(max app - old app)
                        cursorModifier = -bait.getMaxApplications() + (newQuantity - quantity);
                        maxBait = true;
                    } else if (newQuantity != 0) {
                        combined.append(baitName.split(":")[0]).append(":").append(Integer.parseInt(baitName.split(":")[1]) + quantity).append(",");
                        cursorModifier = -quantity;
                    }
                    foundBait = true;
                } else {
                    combined.append(baitName).append(",");
                }
            }

            // We can manage the last character not being a colon if we have to add it in ourselves.
            if (!foundBait) {

                if (getNumBaitsApplied(item) >= EvenMoreFish.baitFile.getMaxBaits()) {
                    // the lore's been taken out, we're not going to be doing anymore here, so we're just re-adding it now.
                    if (doingLoreStuff) {
                        ItemMeta rodMeta = item.getItemMeta();
                        rodMeta.setLore(newApplyLore(item));
                        item.setItemMeta(rodMeta);
                    }
                    throw new MaxBaitsReachedException("Max baits reached.", new ApplicationResult(item, cursorModifier));
                }

                if (quantity > bait.getMaxApplications() && bait.getMaxApplications() != -1) {
                    cursorModifier = -bait.getMaxApplications();
                    combined.append(bait.getName()).append(":").append(bait.getMaxApplications());
                    maxBait = true;
                } else {
                    combined.append(bait.getName()).append(":").append(quantity);
                    cursorModifier = -quantity;
                }
            } else {
                if (combined.length() > 0) {
                    combined.deleteCharAt(combined.length() - 1);
                }
            }
            NBTCompound emfCompound = nbtItem.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
            if (combined.length() > 0) {
                emfCompound.setString(NbtUtils.Keys.EMF_APPLIED_BAIT, combined.toString());
            } else {
                emfCompound.removeKey(NbtUtils.Keys.EMF_APPLIED_BAIT);
            }
        } else {
            nbtItem = new NBTItem(item);
            NBTCompound emfCompound = nbtItem.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
            if (quantity > bait.getMaxApplications() && bait.getMaxApplications() != -1) {
                combined.append(bait.getName()).append(":").append(bait.getMaxApplications());
                emfCompound.setString(NbtUtils.Keys.EMF_APPLIED_BAIT, combined.toString());
                cursorModifier = -bait.getMaxApplications();
                maxBait = true;
            } else {
                combined.append(bait.getName()).append(":").append(quantity);
                emfCompound.setString(NbtUtils.Keys.EMF_APPLIED_BAIT, combined.toString());
                cursorModifier = -quantity;
            }
        }

        item = nbtItem.getItem();

        if (doingLoreStuff && combined.length() >= 1) {
            ItemMeta meta = item.getItemMeta();
            meta.setLore(newApplyLore(item));
            item.setItemMeta(meta);
        }

        if (maxBait) {
            throw new MaxBaitReachedException(bait.getName() + " has reached its maximum number of uses on the fishing rod.", new ApplicationResult(item, cursorModifier));
        }

        return new ApplicationResult(item, cursorModifier);
    }

    /**
     * This fetches a random bait applied to the rod, based on the application-weight of the baits (if they exist). The
     * weight defaults to "1" if there is no value applied for them.
     *
     * @param fishingRod The fishing rod.
     * @return A random bait applied to the fishing rod.
     */
    public static Bait randomBaitApplication(ItemStack fishingRod) {
        if (fishingRod.getItemMeta() == null) return null;

        NBTItem nbtItem = new NBTItem(fishingRod);
        String[] baitNameList = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_APPLIED_BAIT).split(",");
        List<Bait> baitList = new ArrayList<>();

        for (String baitName : baitNameList) {

            Bait bait;
            if ((bait = EvenMoreFish.baits.get(baitName.split(":")[0])) != null) {
                baitList.add(bait);
            }

        }

        double totalWeight = 0;

        // Weighted random logic (nabbed from stackoverflow)
        for (Bait bait : baitList) {
            totalWeight += (bait.getApplicationWeight());
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < baitList.size() - 1; ++idx) {
            r -= baitList.get(idx).getApplicationWeight();
            if (r <= 0.0) break;
        }

        return baitList.get(idx);
    }

    /**
     * Calculates a random bait to throw out based on their catch-weight. It uses the same weight algorithm as
     * randomBaitApplication, using the baits from the main class in the baits list.
     *
     * @return A random bait weighted by its catch-weight.
     */
    public static Bait randomBaitCatch() {
        double totalWeight = 0;

        List<Bait> baitList = new ArrayList<>(EvenMoreFish.baits.values());

        // Weighted random logic (nabbed from stackoverflow)
        for (Bait bait : baitList) {
            totalWeight += (bait.getCatchWeight());
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < EvenMoreFish.baits.size() - 1; ++idx) {
            r -= baitList.get(idx).getCatchWeight();
            if (r <= 0.0) break;
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
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;

        NBTItem nbtItem = new NBTItem(itemStack);
        String[] baitList = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_APPLIED_BAIT).split(",");

        for (String appliedBait : baitList) {
            if (appliedBait.split(":")[0].equals(bait)) return true;
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
        NBTItem nbtItem = new NBTItem(itemStack);
        if (Boolean.FALSE.equals(NbtUtils.hasKey(nbtItem, NbtUtils.Keys.EMF_APPLIED_BAIT)))
            return 0;

        int totalDeleted = 0;
        String[] baitList = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_APPLIED_BAIT).split(",");
        for (String appliedBait : baitList) {
            totalDeleted += Integer.parseInt(appliedBait.split(":")[1]);
        }

        NBTCompound nbtCompound = nbtItem.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
        nbtCompound.removeKey(NbtUtils.Keys.EMF_APPLIED_BAIT);

        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
        return totalDeleted;
    }

    public static List<String> newApplyLore(ItemStack itemStack) {
        if (itemStack.getItemMeta() == null) return Collections.emptyList();
        ItemMeta meta = itemStack.getItemMeta();

        List<String> lore = meta.getLore();
        if (lore == null)
            lore = new ArrayList<>();

        List<String> format = EvenMoreFish.baitFile.getRodLoreFormat();
        for (String lineAddition : format) {
            if (lineAddition.equals("{baits}")) {
                NBTItem nbtItem = new NBTItem(itemStack);

                String rodNBT = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_APPLIED_BAIT);

                if (rodNBT == null || rodNBT.isEmpty())
                    return lore;

                int baitCount = 0;

                for (String bait : rodNBT.split(",")) {
                    baitCount++;
                    lore.add(new OldMessage()
                            .setMSG(EvenMoreFish.baitFile.getBaitFormat())
                            .setAmount(bait.split(":")[1])
                            .setBait(getBaitFormatted(bait.split(":")[0]))
                            .toString());
                }

                if (EvenMoreFish.baitFile.showUnusedBaitSlots()) {
                    for (int i = baitCount; i < EvenMoreFish.baitFile.getMaxBaits(); i++) {
                        lore.add(FishUtils.translateHexColorCodes(EvenMoreFish.baitFile.unusedBaitSlotFormat()));
                    }
                }
            } else {
                lore.add(new OldMessage()
                        .setMSG(lineAddition)
                        .setCurrBaits(Integer.toString(getNumBaitsApplied(itemStack)))
                        .setMaxBaits(Integer.toString(EvenMoreFish.baitFile.getMaxBaits()))
                        .toString());
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
        if (!itemStack.hasItemMeta() || itemStack.getItemMeta() == null || !itemStack.getItemMeta().hasLore())
            return Collections.emptyList();

        List<String> lore = itemStack.getItemMeta().getLore();
        if (lore == null)
            return Collections.emptyList();

        if (EvenMoreFish.baitFile.showUnusedBaitSlots()) {
            // starting at 1, because at least one bait replacing {baits} is repeated.
            for (int i = 1; i < EvenMoreFish.baitFile.getMaxBaits() + EvenMoreFish.baitFile.getRodLoreFormat().size(); i++) {
                lore.remove(lore.size() - 1);

            }
        } else {
            // starting at 1, because at least one bait replacing {baits} is repeated.
            for (int i = 1; i < getNumBaitsApplied(itemStack) + EvenMoreFish.baitFile.getRodLoreFormat().size(); i++) {
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
        NBTItem nbtItem = new NBTItem(itemStack);

        String rodNBT = NbtUtils.getString(nbtItem, NbtUtils.Keys.EMF_APPLIED_BAIT);
        if (rodNBT == null) return 1;

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
        Bait bait = EvenMoreFish.baits.get(baitID);
        if (Objects.equals(bait.getDisplayName(), null)) return baitID;
        else return FishUtils.translateHexColorCodes(bait.getDisplayName());
    }
}