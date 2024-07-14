package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.gui.Button;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIConfig extends ConfigBase {

    private static GUIConfig instance = null;

    public GUIConfig() {
        super("guis.yml", "guis.yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }
    
    public static GUIConfig getInstance() {
        return instance;
    }

    public String getToggle(boolean toggleState) {
        if (toggleState) return getConfig().getString("enabled-msg", "&a&l✔");
        else return getConfig().getString("disabled-msg", "&c&l✘");
    }

    public String getMaterial(boolean toggleState) {
        if (toggleState) return getConfig().getString("enabled-icon", "TROPICAL_FISH");
        else return getConfig().getString("disabled-icon", "SALMON");
    }

    public String getGUIName(String gui) {
        return getConfig().getString(gui + ".title", "&#55aaffEvenMoreFish GUI");
    }

    public List<Button> getButtons(@NotNull final UUID uuid) {
        List<Button> buttons = new ArrayList<>();
        Section section = getConfig().getSection("main-menu");
        if (section == null) return buttons;
        for (String value : section.getRoutesAsStrings(false)) {
            Message materialIcon = new Message(getConfig().getString("main-menu." + value + ".item", "BARRIER"));
            materialIcon.setToggleIcon(getMaterial(!EvenMoreFish.getInstance().getDisabledPlayers().contains(uuid)));
            Button button = new Button(
                    value,
                    uuid,
                    materialIcon.getRawMessage(true),
                    getConfig().getString("main-menu." + value + ".name"),
                    getConfig().getStringList("main-menu." + value + ".lore"),
                    getConfig().getInt("main-menu." + value + ".slot", -1)
            );
            buttons.add(button);
        }
        return buttons;
    }

}
