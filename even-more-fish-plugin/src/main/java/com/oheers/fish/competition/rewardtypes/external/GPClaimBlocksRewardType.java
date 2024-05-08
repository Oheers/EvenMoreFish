package com.oheers.fish.competition.rewardtypes.external;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class GPClaimBlocksRewardType implements RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        int rewardBlocks;
        try {
            rewardBlocks = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        PlayerData data = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        int currentBonus = data.getBonusClaimBlocks();
        data.setBonusClaimBlocks(currentBonus + rewardBlocks);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "GP_CLAIM_BLOCKS";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FireML";
    }

    @Override
    public @NotNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
