package com.oheers.fish.events;

import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AureliumSkillsFishingEvent implements Listener {

    private static final AureliumSkillsFishingEvent aurelliumEvent = new AureliumSkillsFishingEvent();

    public static AureliumSkillsFishingEvent getInstance() {
        return aurelliumEvent;
    }

    @EventHandler
    public void fishCatch(PlayerLootDropEvent event) {
        if (event.getCause() == LootDropCause.LUCKY_CATCH || event.getCause() == LootDropCause.TREASURE_HUNTER || event.getCause() == LootDropCause.EPIC_CATCH || event.getCause() == LootDropCause.FISHING_OTHER_LOOT) {
            if (MainConfig.getInstance().disableAureliumSkills()) {
                if (MainConfig.getInstance().isCompetitionUnique()) {
                    if (EvenMoreFish.getInstance().getActiveCompetition() != null) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }
}
