## The file
This is the file for creating, modifying or deleting fish: if you're running the server locally or viewing the files on a panel through FTP/SFTP, you can use a text-editor such as [Notepad++](https://notepad-plus-plus.org/). At some point in the very distant future I plan to add in a way of modifying this file using a web-editor like LuckPerms uses, but right now (and probably for quite a while) it's just the basic text editor.


<details>
  <summary><i>Default Fish ( click to open )</i></summary>

### Common:
  
* Herring
* Cod
* Sea Bass
* Shrimp
* Anchovies
* Tuna
* Carp
* Sardine
* Sea Snail
* Salmon
* Stick
* Fishing Rod
* Coalfish
* Sanfish

### Uncommon:

* Silver Carp
* Stonefish
* Starfish
* Eel
* Swordfish
* Electric Eel
* Squid
* Pufferfish
* Dory
* Nemo
* Coral
* Jellyfish

### Epic:

* Parrotfish
* Frog
* Clownfish
* Shark Fin
* Dolphin
* Elephant Fish
* Hermit Crab
* Glowsquid
* Turtle

### Legendary:

* Breaded Fish
* Goldfish
* Golden Skull
* Massive Goldfish
* Moustache Pufferfish
* MrCrayfish
* Blue Whale
* Golden Axolotl
* Flower Fish
  
</details>

## Creating fish
Fish are the most customisable feature of this plugin, which is a good job considering this is a fishing plugin! This does however mean that there's so many values its almost a skill within itself to add fish in. Hence why this wiki exists, if you intend to regularly add fish to your server I recommend the `CTRL + B` hotkey to bookmark this page to your browser. If you need any help at any point, feel free to send a message in #help-chat in [the discord help server](https://discord.gg/Hb9cj3tNbb) for further support.

***

    fish:
This is the main section for adding, modifying or deleting fish: this bit should contain a list of rarities that then have a list of fish for that rarity, looking something along the lines of this:

![A stock example of the fish: category in fish.yml](https://media.discordapp.net/attachments/723194663519125618/925118988579668018/unknown.png)

If you would like a default fish (using just the COD) material, putting `Fish: ""` will have the plugin generate a default fish for you - you may wish to use this feature if you're adding in lots of common fish and don't want to have to specify the material, size or lore: each of which can be modified independently to create a unique variety of fish for your server.

***

### The Fish Themselves
#### Info
The options for fish will follow as a long list, alphabetically sorted for ease of finding however you can use `CTRL + F` to search for keywords in this wiki page. None of these options are required (apart from a few which depend upon each other), so you can mix and match to create your ideal fish!

#### The Settings
##### `requirements:`
See [Requirements](https://github.com/FireML/EvenMoreFish/wiki/Requirements)

##### `allowed-regions:`

This setting **requires** that you have either RedProtect or WorldGuard installed on your server, this specifies the regions which the fish can only be found in - if the player is in no region or a region not specified in the allowed-regions value, the plugin either choose a different fish or no fish at all: you are able to specify more than one region

![The allowed-regions option in the config.](https://media.discordapp.net/attachments/723194663519125618/925130501512122468/unknown.png)

##### `catch-event:`

The rewards you specify here will be carried out whenever the fish is caught - this can let you set multiple effects or messages to be sent through the use of commands. It's formatted exactly the same as competition rewards, which will have its own wiki guide at some point.

##### `comp-check-exempt:`

This lets fish appear without competitions occurring - this is only really worth doing if you have fish set to only appear in competitions in your config.yml. Fish set in this value will still be just as likely to occur, as weights are respected: e.g. if you have 4 fish added across 2 rarities with equal weights, there'll be a 25% chance to catch a fish with this value with a competition going on and without a competition going on.

##### `custom-model-data:`

This allows you to set custom model data for your fish, if you wish to use a resource pack this feature will be incredibly useful for you. If the plugin is released for pre-1.14 versions in the future, you will not be able to use this feature. For more information on custom-model-data, visit [this forum post](https://www.planetminecraft.com/forums/communities/texturing/new-1-14-custom-item-models-tuto-578834/).

![Custom-model-data in the fish.yml file](https://media.discordapp.net/attachments/723194663519125618/926074666525810698/unknown.png)

##### `displayname:`

This is the displayname of the fish, if this option is set it will override the name that the fish is normally given, which allows you to give the fish name some colour if you wish, this replaces the fish's name everywhere (e.g. catching, the item itself, in the leaderboard ...)

##### `durability:`

This takes an integer value for the percentage of damage the fish should have (this is only valid for tools that can be damaged) - 1% would be a very broken tool whereas 100% would be a tool with no damage indicator at all. This does not apply to fish obtained from /emf admin fish, only ones that were caught using a fishing rod.

##### `dye-colour:`

If your fish material is dyable (leather / horse armour) you can use this to set the hex value of the colour. You must use quotes around your value to prevent it being read as a comment and therefore ignored and breaking your config.

![dye-colour in use in config.](https://media.discordapp.net/attachments/723194663519125618/925372880936640522/unknown.png)

##### `eat-event:`

If your fish is a consumable item such as a salmon, the rewards you specify here will be carried out when the fish is eaten. It's formatted exactly the same as competition rewards, which will have its own wiki guide at some point.

##### `effect:`

This applies a potion effect to a player, in the format EFFECT_NAME:AMPLIFIER:DURATION. For example, a fish which gives you speed I for 5 seconds would be formatted like so: `effect: SPEED:1:5`. Using this method, you can only give one effect however using `catch-event` you're able to run multiple commands to add the effects to the player. 

![The config of giving a user speed I for 5 seconds.](https://media.discordapp.net/attachments/723194663519125618/925364020805001246/unknown.png)

![The in-game representation of having Speed I](https://media.discordapp.net/attachments/723194663519125618/925364721404743730/unknown.png)

##### `interact-event:`

If your fish is not a consumable item, and the player right clicks the air whilst not shifting, the rewards you specify here will be carried out when the fish is eaten. It's formatted exactly the same as competition rewards, which will have its own wiki guide at some point.

##### `item:`

This bit has [its own section.](https://github.com/Oheers/EvenMoreFish/wiki/Fish.yml/_edit#the-item-section)

##### `glowing: `

This adds a useless enchantment to the fish (unbreaking 1) and hides this enchantment from the player, so it just appears to be glowing for them. This will not be compatible with any future release that allows you to add enchantments to the fish due to the fact it hides the enchantments.

In this photo, the first fish is non-glowing (`glowing: false`) and the second is glowing (`glowing: true`).

![An image comparing a glowing fish with a non-glowing fish.](https://media.discordapp.net/attachments/723194663519125618/925138365114908723/unknown.png)

##### `lore:`

This adds extra lore to the fish, that can be used to give information about the fish or hints for what can happen if they eat/interact using it. This is structured in list format and added underneath the fish info.

![Config and in-game representation of the fish's lore.](https://media.discordapp.net/attachments/723194663519125618/925366965978161193/unknown.png)

##### `disable-lore:`

##### `disable-fisherman:`

##### `message:`

This is a message sent when a player catches this fish, there's not much more to it in all honesty - other than the escape character `\n` lets you start on a new line.

![Config for a message being sent.](https://media.discordapp.net/attachments/723194663519125618/925358694814875668/unknown.png)

![An example of a message being sent](https://media.discordapp.net/attachments/723194663519125618/925358409585410058/unknown.png)

##### `size:`

This one requires two values coming out of it as well, `minSize` and `maxSize`: like rarities.yml, this is the minimum size & length for the individual fish, and this will take priority over the value set in rarities.yml. By setting minSize to -1, you can disable the size of the fish completely - however the fish then can't be sold in /emf shop or enter the fishing competitions (unless it's the [MOST_FISH](https://github.com/Oheers/EvenMoreFish/wiki/Competition-Types#most_fish) competition type)

![An image demonstrating the minSize and maxSize set.](https://media.discordapp.net/attachments/723194663519125618/925327899773526076/unknown.png)

##### `weight:`

When adding this, it is **imperative** you apply it to all fish in this fish's rarity - otherwise this will be the only fish to appear and it'll be pointless. A higher value of weight means the fish is more likely to appear and a lower value means it's less likely to appear, the total sum of all the fish's weights in the rarity does **not** have to add up to 100, it can be any number at all. (I did try to find a webpage but none were very relevant to what was needed)

#### The "Item" Section

This section explains the `item` value, which is so intricate I decided to give it its own part. Whatever follows the `item:` can be followed by any of 6 different values that define which item the fish will be, you can only choose one and can't mix and match. 

##### `material:`

This is the one you'll probably be using the most, it specifies that the fish will be set to an in-game material, such as COD, SALMON, or any other material that's shown in the `F3 + H` description of items when you hover over them with this enabled. A list of the materials allowed can be found on [the spigot javadocs.](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html).

![An image showing the F3 + H value of items in-game.](https://media.discordapp.net/attachments/723194663519125618/925335024365088798/Screenshot_2021-12-28_102707.png)

This is used in one of the default generated fish (Coalfish)'s material.

![The config value alongside the in-game representation.](https://media.discordapp.net/attachments/723194663519125618/925337663295336468/Untitled-1.png)

##### `head-64:`

This represents the base-64 head value that all player skulls are assigned by mojang. It should be a very long string of seemingly random letters and numbers. You can use [the minecraft-heads website`](https://minecraft-heads.com/custom-heads/) to get pre-made heads, make sure to copy this value here, at the very bottom of the webpage:

![minecraft-heads website base-64 code.](https://media.discordapp.net/attachments/723194663519125618/925341210455965726/unknown.png)

You can also upload your own skin to [MineSkin](https://mineskin.org/) and copy the code under the "Texture Value" field.

You should put this value in quotations after the `head-64:` so it looks like this, but for your code:

![head-64 in config.](https://media.discordapp.net/attachments/723194663519125618/925342772087640104/unknown.png)

##### `head-uuid:`

This also sets the fish to a player head, but using a player's UUID rather than the base-64 code. This will only work **if the player has previously joined the server**, and the player's UUID can be fetched from their name using [NameMC](https://namemc.com/) and typing their in-game name into the search bar and copying this code.

![Oheers' UUID on NameMC](https://media.discordapp.net/attachments/723194663519125618/925348350511951893/Screenshot_2021-12-28_112238.png)

##### `own-head:`

This lets you give the player their own head, unlike the other ones that take a value, this takes in a `true` value. So you would set it up like: `own-head: true`. 

![A head using the own-head setting](https://media.discordapp.net/attachments/723194663519125618/1000029154789568552/unknown.png)

##### `raw-material:`

This will return just a default item to come out of the water as a normal fish would. A material is inputted but when a user catches it no lore is added, no nbt data. As a result, this **cannot** be sold in /emf shop, and must not be used for baits as it would make them completely useless.

`materials:`
`multiple-head-64:`
`multiple-head-uuid:`

These are all for specifying multiple possible options for each fish, the plugin will choose a random value from the values you give to it for each time somebody catches this fish.

![A set-up where coral is given a random coral item.](https://media.discordapp.net/attachments/723194663519125618/925351313506705418/unknown.png)

This is a setup for coral fish, you can have unlimited options for the fish, and it's the same for both head-64 and head-uuid, just with the UUID/base-64 instead. All the options are equally weighted, meaning they're equally likely to appear.

##### `potion:`
And finally, there is the potion modifier, this lets you set which potion effects this fish should give, which is useful if you want to give potions as fish. This should be formatted as **effect:duration(s):amplifier**, a list of valid potion effects can be found [on the spigot javadocs.](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html)

![An image representing how to make a potion fish.](https://media.discordapp.net/attachments/723194663519125618/954420186851733575/Screenshot_2022-03-18_164126.png)

(This creates a potion with absorption 1 for 1 second.)