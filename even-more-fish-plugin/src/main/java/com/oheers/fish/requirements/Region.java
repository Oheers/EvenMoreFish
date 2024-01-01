package com.oheers.fish.requirements;

import com.oheers.fish.utils.FishUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Region implements Requirement {

    public final String configLocation;
    public final FileConfiguration fileConfig;
    public List<String> regions = new ArrayList<>();

    /**
     * Makes sure the player is stood in one of a defined list of regions to allow them to catch the fish, currently
     * WorldGuard and RedProtect are supported. The location variable must not be null in order to carry out this check,
     * as well as one of the two aforementioned plugins being installed on the server.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "region:"
     *                       for example, "fish.Common.Herring.requirements.region".
     * @param fileConfig The file configuration to fetch file data from, this is either the rarities or fish.yml file,
     *                   but it would be possible to use any file, as long as the configLocation is correct.
     */
    public Region(@NotNull final String configLocation, @NotNull final FileConfiguration fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        return FishUtils.checkRegion(context.getLocation(), regions);
    }

    @Override
    public void fetchData() {
        regions.addAll(fileConfig.getStringList(this.configLocation));
    }
}
