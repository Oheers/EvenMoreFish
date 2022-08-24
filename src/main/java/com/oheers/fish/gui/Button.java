package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

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
    public Button(@NotNull final String identifier, @NotNull final UUID viewer, @NotNull final String materialString, @NotNull final String name,
                  @NotNull final List<String> unformattedLore, final int slot) {
        this.identifier = identifier;

        try {
            this.material = Material.valueOf(materialString);
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, "Invalid material ("  + materialString + ") for gui button: " + identifier);
            this.material = Material.BARRIER;
        }

        Message toggleVAR = new Message(name);
        toggleVAR.setToggleMSG(EvenMoreFish.guiConfig.getToggle(EvenMoreFish.disabledPlayers.contains(viewer)));
        this.name = toggleVAR.getRawMessage(true, true);

        this.lore = unformattedLore;
        for (int i = 0; i < unformattedLore.size(); i++) {
            this.lore.set(i, new Message(unformattedLore.get(i)).getRawMessage(true, false));
        }

        this.slot = slot;
    }

}
