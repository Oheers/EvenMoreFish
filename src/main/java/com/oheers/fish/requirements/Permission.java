package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class Permission implements Requirement {

    public final String configLocation;
    public final FileConfiguration fileConfig;
    public String permissionNode;

    /**
     * Just like the old permission checker, if the user doesn't have the correct permission node or isn't op then the
     * fish won't be given. This requires a permission plugin to exist though such as LuckPerms, as well as Vault.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "permission:"
     *                       for example, "fish.Common.Herring.requirements.permission".
     * @param fileConfig The file configuration to fetch file data from, this is either the rarities or fish.yml file,
     *                   but it would be possible to use any file, as long as the configLocation is correct.
     */
    public Permission(@NotNull final String configLocation, @NotNull final FileConfiguration fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        if (EvenMoreFish.permission != null && permissionNode != null) {
            return context.getPlayer() == null || EvenMoreFish.permission.has(context.getPlayer(), permissionNode);
        } else {
            return true;
        }
    }

    @Override
    public void fetchData() {
        this.permissionNode = fileConfig.getString(configLocation);
    }
}
