## How to create your own RewardTypes

The [RewardType](https://github.com/Oheers/EvenMoreFish/blob/master/even-more-fish-api/src/main/java/com/oheers/fish/api/reward/RewardType.java) interface exists in the API module of EvenMoreFish. Below is an example of how to use it.

```
/**
 * This is the EXP RewardType that exists inside EvenMoreFish.
 */
public class EXPRewardType implements RewardType {

    /**
     * The reward itself is handled here.
     * @param player The player this reward is meant for.
     * @param key The key of this reward, should match the identifier.
     * @param value The value of this reward. For example EXP:5000, 5000 is the value.
     * @param hookLocation The location of the player's fishing hook.
     */
    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        int experience;
        try {
            experience = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        player.giveExp(experience);
    }

    /**
     * The identifier of this RewardType.
     * If a RewardType already exists with this identifier, this type will not be registered.
     */
    @Override
    public @NotNull String getIdentifier() {
        return "EXP";
    }

    /**
     * The author of this RewardType.
     * This is shown in the admin command, and is intended to credit the author.
     */
    @Override
    public @NotNull String getAuthor() {
        return "FireML";
    }

    /**
     * The plugin responsible for this RewardType.
     * This is also shown in the admin command.
     */
    @Override
    public @NotNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
```

After writing your own class, you can register it with EMF using the RewardType#register method.

