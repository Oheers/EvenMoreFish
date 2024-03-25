package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Permission implements Requirement {

    public final String configLocation;
    public final FileConfiguration fileConfig;
    private List<String> permissionNodes;

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
        if (EvenMoreFish.getInstance().getPermission() != null && permissionNodes != null) {
            return context.getPlayer() == null || hasAllPermissions(context.getPlayer());
        }
        
        return true;
    }
    
    private boolean hasAllPermissions(final Player player) {
        for(final String permissionNode: permissionNodes) {
            if(!EvenMoreFish.getInstance().getPermission().has(player,permissionNode)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void fetchData() {
        this.permissionNodes = getPermissionsListOrSingleton();
    }
    
    /*
        Here we account for the old permission format.
        That way there is no need to migrate old .yml files to the new format.
        So users can use either:
        requirement:
          permission: "evenmorefish.fish1"
        or
        requirement:
          permission:
          - "evenmorefish.fish1"
          - "evenmorefish.fish2"
     */
    private @NotNull List<String> getPermissionsListOrSingleton() {
        List<String> permissions = fileConfig.getStringList(configLocation);
        if(permissions.isEmpty()) {
            return Collections.singletonList(fileConfig.getString(configLocation));
        }
        return permissions;
    }
}
