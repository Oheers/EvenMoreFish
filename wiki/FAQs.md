# FAQs

This answers a few questions I recognize are asked pretty frequently on [the discord help server](https://discord.gg/Hb9cj3tNbb):

## How do I add commands when a fish is caught?

This can be done using the `catch-event` setting for the fish.
```yml
Rusty Repair Spoon:
  item:
    material: IRON_SHOVEL
  catch-event:
    - "COMMAND:repair hand {player}"
    - "MESSAGE:&aYour fishing rod has been repaired."
```

## How do I give a fish / bait to another player?

To give a fish to a player, use the following command:
`/emf admin fish Common Bluefish 4 Oheers`

To give a bait to a player, use the following command:
`/emf admin bait Shrimp 1 Oheers`

- Note: If using another plugin, you'll want to replace Oheers with the variable used for the player, this varies from plugin to plugin and will look like [playerName], {player}, {playername} etc.

## How do I change the name of the default fish?

All you have to do is change the name in the .yml files, so

```yml
Blue Shark:
```
Becomes
```yml
Blahaj: 
```
This is exactly the same for rarities - also make sure to also change the name of the fish & rarities in the baits.yml file.
- Note: If you're changing the name of the rarities in rarities.yml you'll also need to change their references in fish.yml. These can be seen on column 3 and are shown where they are in the screenshot in [this section](https://github.com/Oheers/EvenMoreFish/wiki/Fish.yml#creating-fish) of fish.yml
- Note: You should always use english characters for the config keys. To name things in your language, you should use the display name config instead.

## How do I change how much fish sell for in /emf shop?

As explained in the rarities.yml above the setting, each fish's rarity will have a `worth-multiplier` setting, this is then multiplied by the length of the fish to create the value seen in the `/emf shop`. By increasing or decreasing the worth-multiplier, you'll increase/decrease their worth in the shop.

## How do weights work?

A fair amount of people believe weights are the same as percentages, **this is false**: All weights work relative to each other, so if `Fish A` has a weight of 6 and `Fish B` has a weight of 12, `Fish B` will be twice as likely to be caught as `Fish A`. If the weights of all other fish in this rarity aside from `Fish B` (remember, this has weight of 12) add up to 12 too, then `Fish B` will be pulled 50% of the time, but if all other fish in the rarity added to 1,000 for example then `Fish B` would be pretty rare to catch. To calculate the weight of `Fish A` we'll assume that all the fish in the same rarity as `Fish A` adds up to 600, the weight of `Fish A` divided by the total weight in that rarity is 6 / 600, so 1%.

## How do i use ItemsAdder items?

See https://github.com/Oheers/EvenMoreFish/wiki/Addons