package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//todo, not being used??
public class FishingGUI extends Gui {

    List<Button> guiButtons = new ArrayList<>();

    /**
     * Creates a fishing GUI object to render the "/emf" gui. A new one needs to be made for each user otherwise it can
     * cause a mess with the /emf toggle button where the button will change for everyone if one person changes it. Essentially,
     * just make a new object for each user.
     *
     * @param viewer The UUID of the player who will open the GUI.
     */
    public FishingGUI(@NotNull final UUID viewer, @NotNull final FillerStyle fillerStyle) {
        super(viewer, new Message(EvenMoreFish.getInstance().getConfigManager().getGuiConfig().getGUIName("main-menu")));
        loadFiller();
        loadButtons();
    }

    /**
     * Loads the inventory for the player and also changes it to be to the specification of the user (right now that's just
     * the state of /emf toggle.
     * @param player The player to send the inventory to.
     */
    public void display(Player player) {
        for (Button button : this.guiButtons) {
            if (button.getSlot() >= 0) {
                this.inventory.setItem(button.getSlot(), button.getItem());
            }
        }
        player.openInventory(this.inventory);
    }

    public void loadButtons() {
        this.guiButtons = EvenMoreFish.getInstance().getConfigManager().getGuiConfig().getButtons(viewer);
    }

    @Override
    public void loadFiller() {
        EvenMoreFish.getInstance().getConfigManager().getGuiConfig().fillerDefault.forEach(this.inventory::setItem);
    }
}
