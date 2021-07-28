package com.oheers.fish.events;

import com.gmail.nossr50.events.McMMOReplaceVanillaTreasureEvent;
import com.oheers.fish.EvenMoreFish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class McMMOTreasureEvent implements Listener {

    @EventHandler
    public void mcmmoTreasure(McMMOReplaceVanillaTreasureEvent event) {
        if (EvenMoreFish.mainConfig.disableMcMMOTreasure()) {
            if (EvenMoreFish.mainConfig.isCompetitionUnique()) {
                if (EvenMoreFish.active != null) {
                    event.setReplacementItemStack(event.getOriginalItem().getItemStack());
                }
            } else {
                event.setReplacementItemStack(event.getOriginalItem().getItemStack());
            }
        }
    }
}
