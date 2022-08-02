package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.jetbrains.annotations.NotNull;

public class Permission implements Requirement {

    public final String configLocation;
    public String permissionNode;

    /**
     * Just like the old permission checker, if the user doesn't have the correct permission node or isn't op then the
     * fish won't be given. This requires a permission plugin to exist though such as LuckPerms, as well as Vault.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "irl-time:
     *                       for example, "fish.Common.Herring.requirements.permission".
     */
    public Permission(@NotNull final String configLocation) {
        this.configLocation = configLocation;
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
        this.permissionNode = EvenMoreFish.fishFile.getConfig().getString(configLocation);
    }
}
