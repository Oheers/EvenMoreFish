package com.oheers.fish.events;

import com.gmail.nossr50.events.skills.fishing.McMMOPlayerFishingTreasureEvent;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class McMMOTreasureEvent implements Listener {

    @EventHandler
    public void mcmmoTreasure(McMMOPlayerFishingTreasureEvent event) {
        if (EvenMoreFish.mainConfig.disableMcMMOTreasure()) {
            if (EvenMoreFish.mainConfig.isCompetitionUnique()) {
                if (Competition.isActive()) {
                    event.setTreasure(null);
                }
            } else {
                event.setTreasure(null);
            }
        }
    }
}
