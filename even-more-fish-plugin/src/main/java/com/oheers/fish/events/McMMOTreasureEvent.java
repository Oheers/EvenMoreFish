package com.oheers.fish.events;

import com.gmail.nossr50.events.McMMOReplaceVanillaTreasureEvent;
import com.gmail.nossr50.events.skills.fishing.McMMOPlayerFishingTreasureEvent;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.config.MainConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class McMMOTreasureEvent implements Listener {

    private static final McMMOTreasureEvent mcmmoEvent = new McMMOTreasureEvent();

    private McMMOTreasureEvent() {

    }

    public static McMMOTreasureEvent getInstance() {
        return mcmmoEvent;
    }

    @EventHandler
    public void mcmmoTreasure(McMMOReplaceVanillaTreasureEvent event) {
        if (MainConfig.getInstance().disableMcMMOTreasure()) {
            if (MainConfig.getInstance().isCompetitionUnique()) {
                if (CompetitionManager.getInstance().getActiveCompetition() != null) {
                    event.setReplacementItemStack(event.getOriginalItem().getItemStack());
                }
            } else {
                event.setReplacementItemStack(event.getOriginalItem().getItemStack());
            }
        }
    }

    @EventHandler
    public void mcmmoTreasure(McMMOPlayerFishingTreasureEvent event) {
        if (MainConfig.getInstance().disableMcMMOTreasure()) {
            if (MainConfig.getInstance().isCompetitionUnique()) {
                if (CompetitionManager.getInstance().isCompetitionActive()) {
                    event.setTreasure(null);
                }
            } else {
                event.setTreasure(null);
            }
        }
    }
}
