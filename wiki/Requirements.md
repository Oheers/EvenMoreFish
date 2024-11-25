## How do requirements work?
Requirements set a list of rules for a fish to appear and can be applied to any fish to limit fish to only appear when the rule(s) are met. If a fish has no requirements it will always be able to be caught (depending on the allowed-worlds/regions setting in config.yml) however if all fish have requirements and the user is in a situation where they meet none of the requirements for the rarity fish they've caught an error may be sent to console. When a user reels in their rod, the plugin chooses a rarity the player will receive, then it chooses a fish. It doesn't go back and re-roll the rarity if it can't find any fish to give the user, hence why there must be at least one fish in each rarity that a user will always meet requirements for. 

A few settings from pre-1.6 have been moved into requirements, so it's advised to check through this before upgrading.

### Rarity requirements
As of 1.6.10 it's possible to add in requirements to rarities. These are applied exactly the same as they are to fish in the fish.yml file and all requirements available to fish are available to rarities - if a user doesn't meet the criteria for a rarity they won't be able to catch any fish in that rarity at all.

An example of using requirements for a rarity:
```yaml
rarities:
  # ▼ All rarity names should start on the same column as this down arrow.
  Common:
    # How likely is the rarity to be chosen? Having a greater weight means the rarity is more likely to be chosen (the total weights don't have to add to 100)
    weight: 100
    # What colour should the fish's name be?
    colour: '&7'
    # Prices in /emf shop are calculated by using the calculation "length * worth-multiplier". You can change the worth-multiplier here.
    worth-multiplier: 0.1
    # Should a catch of this fish be broadcasted to everyone (true) or just the fisher (false)?
    broadcast: false
    # Change the sizing ranges of the fish.
    size:
      # Absolute minimum size the fish can be, this must be an integer
      minSize: 1
      # Absolute maximum size the fish can be, this must be an integer
      maxSize: 30
    requirements:
      permission: "emf.rarity.common"
```

***

### Requirements

#### Biome
This lets you define a list of biomes the fish will only appear in, allowing you to create fish from cool areas and fish from hot areas. This takes in a <u>list</u> value, allowing for multiple biomes allowed per fish.

A config example of using the biome requirement:
```yaml
    Atlantic Cod:
      requirements:
        biome:
        - COLD_OCEAN
        - DEEP_COLD_OCEAN
        - DEEP_LUKEWARM_OCEAN
        - DEEP_OCEAN
        - LUKEWARM_OCEAN
        - OCEAN
        - WARM_OCEAN
```

- Note: To create groups and reuse them, try Biome Sets (explained below this section).

#### Biome Sets

Biome Sets are configurable in config.yml. Here is the default config for them:
```yaml
biome-sets:
  # oceans biome set. you can add more sets as you please.
  oceans: # This is the name of the biome set.
    - COLD_OCEAN
    - DEEP_COLD_OCEAN
    - DEEP_LUKEWARM_OCEAN
    - DEEP_OCEAN
    - LUKEWARM_OCEAN
    - OCEAN
    - WARM_OCEAN
```

A config example of using the biome set requirement:
```yaml
    Atlantic Cod:
      requirements:
        biome-set:
        - oceans
```

#### Ingame Time
This restricts your users to only catching fish during certain times of in-game day, this time will be inputted based on the current in-game tick, which happens 20 times every second. For example, 15 seconds past midnight would be 20*15 and four minutes would be 4*60*20. Like minSize/maxSize, you need to specify a minTime & maxTime in the format of `min-max`.

A config example of using the ingame-time requirement:
```yaml
    Atlantic Cod:
      requirements:
        ingame-time: 2000-4000 # Any time between the first 2000 ticks (100 seconds) and 4000 ticks (200 seconds) of the ingame day.
```

#### IRL Time
This restricts your users to only catching fish during certain times of the real-world day, this is inputted similar to times for [times in the competitions.yml wiki](https://github.com/Oheers/EvenMoreFish/wiki/competitions.yml#timing) where they are formatted by 00:00 in 24-hour format. Like ingame-time, you need to specify the minimum and maximum time.

A config example of using the irl-time requirement:
```yaml
    Atlantic Cod:
      requirements:
        irl-time: 01:00-03:00 # Any time between 1AM and 3AM in real life.
```

#### Moon Phases
This lets you restrict fish to certain moon phases, it is recommended to combine this with the ingame-time requirement as otherwise the fish may appear during daytime too, when the moon phase is irrelevant. Below is a list of all the moon phase types as referenced in EvenMoreFish and a screenshot from the Minecraft fandom wiki showing all the moon phases.

<details>
  <summary>View the moon phases ( click to open )</summary>

* `FULL_MOON`
* `WANING_GIBBOUS`
* `LAST_QUARTER`
* `WANING_CRESCENT`
* `NEW_MOON`
* `WAXING_CRESCENT`
* `FIRST_QUARTER`
* `WAXING_GIBBOUS`

Moon Phases listed on the Minecraft Wiki: https://minecraft.wiki/w/Moon#Phases

</details>

A config example of using the moon-phase requirement:
```yaml
    Atlantic Cod:
      requirements:
        moon-phase:
          # When the moon phase matches either of these.
          - WAXING_GIBBOUS
          - NEW_MOON
```

#### Permission
Like biome and world requirements, this has moved from its own config setting. If you still have the old `permission:` setting for your fish not in any requirements, this will need to be converted to this new format.

A config example of using the permission requirement:
```yaml
    Atlantic Cod:
      requirements:
        permission: "emf.fish.atlanticcod" # When the player has this permission.
```

#### Region
This lets you limit fish to a specific region(s) to appear in however, this will override your global value for this set in config.yml, for example, if you whitelist "region" in your config.yml but don't include it in your region requirement, the fish won't be able to spawn in that region. 
[This requires WorldGuard or RedProtect]

A config example of using the region requirement:
```yaml
    Atlantic Cod:
      requirements:
        region: FishingRegion # When the player is inside this region.
```

#### Weather
This limits fish to only appear in certain weather. Initially when I started adding requirements I thought there was a lot more weather variation. There was not. As it turns out, your choice is limited to `CLEAR` or `DOWNFALL`. For non-English speakers I can imagine this quite easily getting lost in translation, so simply `CLEAR` is not raining/snowing, and `DOWNFALL` is raining/snowing (Minecraft sees rain and snow as the same thing)

A config example of using the weather requirement:
```yaml
    Atlantic Cod:
      requirements:
        weather: DOWNFALL # When it is raining or snowing.
```

#### World
And finally, this limits fish to certain worlds. By default there's only three worlds, `overworld`, `nether` and `end`, but with a plugin like [Multiverse-Core](https://www.spigotmc.org/resources/multiverse-core.390/), you can create worlds to allow fish to only be caught in. There probably isn't much point allowing `nether` since water can't be placed there and lava can't be fished in though.

A config example of using the world requirement:
```yaml
    Atlantic Cod:
      requirements:
        world: FishingWorld # When the player is in this world.
```