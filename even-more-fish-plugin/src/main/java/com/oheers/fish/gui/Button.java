package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.selling.WorthNBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Button {

    final String identifier;
    Material material;
    String name;
    List<String> lore;
    int slot;

    /**
     * Represents an item to be placed in a GUI, this doesn't necessarily have to have something happen when clicked (an
     * example being the filler items. These will be loaded from the guis.yml file usually.
     *
     * @param identifier The name of the button, recognized in the gui.yml file.
     * @param materialString A string version of the material, this will check if it's an ok string.
     */
    public Button(@NotNull final String identifier, @NotNull final UUID viewer, @NotNull final String materialString, final String name,
                  @NotNull final List<String> unformattedLore, final int slot) {
        this.identifier = identifier;

        try {
            this.material = Material.valueOf(materialString);
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance().getLogger().severe("Invalid material ("  + materialString + ") for gui button: " + identifier);
            this.material = Material.BARRIER;
        }

        if (name != null) {
            Message toggleVAR = new Message(name);
            toggleVAR.setToggleMSG(GUIConfig.getInstance().getToggle(!EvenMoreFish.getInstance().getDisabledPlayers().contains(viewer)));
            this.name = toggleVAR.getRawMessage(true);
        }

        if (!unformattedLore.isEmpty()) {
            this.lore = unformattedLore;
            for (int i = 0; i < unformattedLore.size(); i++) {
                this.lore.set(i, new Message(unformattedLore.get(i)).getRawMessage(false));
            }
        }

        this.slot = slot;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(this.material);
        if (item.getItemMeta() != null) {
            ItemMeta itemMeta = item.getItemMeta();
            if (this.name != null) itemMeta.setDisplayName(new Message(this.name).getRawMessage(false));
            if (!this.lore.isEmpty()) itemMeta.setLore(new Message(this.lore).getRawListMessage(false));
            item.setItemMeta(itemMeta);
            return WorthNBT.attributeDefault(item);
        }
        return item;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getSlot() {
        return slot;
    }
}
