package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Region implements Requirement {

    public final String configLocation;
    public List<String> regions = new ArrayList<>();

    /**
     * Makes sure the player is stood in one of a defined list of regions to allow them to catch the fish, currently
     * WorldGuard and RedProtect are supported. The location variable must not be null in order to carry out this check,
     * as well as one of the two aforementioned plugins being installed on the server.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "irl-time:
     *                       for example, "fish.Common.Herring.requirements.region".
     */
    public Region(@NotNull final String configLocation) {
        this.configLocation = configLocation;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        return FishUtils.checkRegion(context.getLocation(), regions);
    }

    @Override
    public void fetchData() {
        regions.addAll(EvenMoreFish.fishFile.getConfig().getStringList(this.configLocation));
    }
}
