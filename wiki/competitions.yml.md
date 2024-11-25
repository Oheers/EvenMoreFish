## The file
This is the file for creating new competitions, if you're using a version below v1.4 and are looking to upgrade, it's important you modify this file to match your competitions set in the config.yml, otherwise the default competition settings will begin, giving potentially undesired results. If you're running the server locally or viewing the files on MultiCraft through FTP/SFTP, you can use a text-editor such as [Notepad++](https://notepad-plus-plus.org/).

## Creating competitions
You can create (within reason) as many scheduled competitions as you want in your competitions.yml file, by default there's 4 competitions and general settings below. This page contains a highly detailed guide on how to setup the most important feature of the EvenMoreFish plugin - if you need any help, feel free to send a message in #help-chat in [the discord help server](https://discord.gg/Hb9cj3tNbb) for further support.

***

    competitions:
This is the main section for adding, modifying or deleting competitions: to create a new competition, write the competition name, followed by a colon. Congratulations, you've made your first competition! It should look like this:

![new competitions made in the competitions.yml file](https://media.discordapp.net/attachments/723194663519125618/883271824715165706/unknown.png)

In the current version of EvenMoreFish (v1.4), the competition name doesn't really matter (as long as it's unique to other competition names) as players won't see it. However, we do need to add some settings to allow our new competition to run: any value that's <u>`underlined`</u> is **required** to allow the competition to begin without errors in the console.

<u>`type:`</u>

This is the type of competition you want it to be, if you're not sure what this means there's [a whole wiki page](https://github.com/Oheers/EvenMoreFish/wiki/Competition-Types) about them. Using any of the values specified in that page are allowed (`LARGEST_FISH`, `MOST_FISH` or `SPECIFIC_FISH`)

<u>`duration:`</u>

This is how long the competition will last **in minutes**, you cannot use decimal places to denote individual seconds, nor can you use negative numbers (obviously)

***

### Timing
#### Option 1
There's 2 ways of doing timing, you can still use the old system however there's a few modifications to how this works, the first one is the  change to day names rather than numbers.

`blacklisted-days:`

As explained in the above paragraph, you'll need to use the actual day name rather than the day's numeric value. The days must be spelt out in capitals, otherwise this setting won't work. If you wanted a MOST_FISH competition to only run on weekdays, it would look like this:

![A competition set to only running on weekdays](https://media.discordapp.net/attachments/723194663519125618/994240422996091010/unknown.png)

<u>`times:`</u>
> This is only required if you're going with option 1.

These are the times the plugin will run, they follow the 24 hour format, and you can have as many of these as you like (as long as they won't run at the same time as another competition). Once you've set these times, the plugin will automatically start the competition at each time on each day of the week you haven't added to `blacklisted-days`. If you wanted your competition to run at 4 AM, 12 NOON and 4PM on weekdays it should look like this:

![a competition starting at 4am, 12noon and 4pm](https://media.discordapp.net/attachments/723194663519125618/994240880875679804/unknown.png)

#### Option 2

This is the new way of starting competitions from the new v1.4 release. It lets you set different starting times dependant on the day, for example you may want a competition to run 6 times on Saturday but only 2 times on Sunday.

<u>`days:`</u> 
> This is only required if you're going with option 2.

This is the main section for setting days. Like `blacklisted-days`, it uses the day name rather than numeric value - however it's as a configuration section rather than string in a list. Therefore, it looks like this:

![a competition only running on MON, THU & FRI](https://media.discordapp.net/attachments/723194663519125618/883286425729130496/unknown.png)

The competition will only run on the days you set, and will be ignored on days that aren't present in this list. In addition to this, you must also add times for the competition to run, each day you added can have its own set times for it to run - these times need to be in the 24 hour format for the plugin to be able to load them in. An example would be:

![a competition with different times for each day](https://media.discordapp.net/attachments/723194663519125618/883300468057587712/unknown.png)

***

### Miscellaneous

These options are mostly optional, but allow you to customize each competition even more than before the v1.4 release, if you don't have them present, it will just use the values set in the general section. They can also go anywhere in the config for the competition you're making, the same goes for all other values in this wiki.

`bossbar-colour:`

This sets the colour for the bossbar that is displayed to all players during a competition. This could be used to help your players better identify which [competition type](https://github.com/Oheers/EvenMoreFish/wiki/Competition-Types) is running, or as a unique marker for which different competition it is, which would prove useful if you're running specific rewards for the competition.

![the bossbar colour being set to yellow]()

![a yellow bossbar](https://github.com/Oheers/EvenMoreFish/assets/4803946/1c433f5f-8192-4972-9cef-5b73cffa8e9d)

`bossbar-prefix:`

You may have noticed in the above screenshot that the next remained green whilst the bossbar was yellow, if you like this - it's fine to leave it without this value as it's optional, however if you're like me and would rather have both colours the same: this value will come in handy. It represents what comes before the "9m 43s left" and defaults to the standard green "Fishing Contest: ", (you need a space at the end unless you want the prefix to be right next to the timer). The value looks like this:

![a competition with a modified bossbar prefix](https://media.discordapp.net/attachments/723194663519125618/883306833744388136/unknown.png)

![competition with modified bossbar in action](https://media.discordapp.net/attachments/723194663519125618/883307107464642570/unknown.png)

`alerts:`

This is also a new feature in v1.4, where all online players will be sent a message of your choosing at times of your choosing, these follow a format that looks like the 24hour format but is not entirely the same, instead using a MM:SS format, where 20:00 would send the message from `time-alert` in messages.yml (line:81 by default) when there are 20 minutes remaining. You can set as many of these as you want at different periods during the competition, and each competition can have individual timings. This can't be put in the general category for all competitions, so you'll need to copy & paste it into every competition you're using. Make sure that you aren't sending alerts to players at times greater than the competition duration i.e. a message at "30:00" wouldn't be suitable for a 10minute long competition.

![times for competition alerts](https://media.discordapp.net/attachments/723194663519125618/883372585939771432/unknown.png)

`minimum-players:`

This is the minimum number of players needed for the competition to run. A message will be broadcasted to all online players if there aren't enough players online.

![There's not enough players online to start the scheduled fishing competition.](https://media.discordapp.net/attachments/723194663519125618/883381011180822528/unknown.png)

`rewards:`

This allows you to set custom rewards for each competition - more information about this can be found in the general section of this page.

<u>`allowed-rarities:`</u>

> Only needed if using the SPECIFIC_FISH competition type.

These are the list of rarities that SPECIFIC_FISH will be able to choose a random fish from. To make the competition harder, you can remove your most common rarities from this list, or to make it easier you can remove the rarest rarities from the list. Again, this can't be put in the general section - but it takes a list; in this example, only uncommon fish can be selected during the competition.

![only uncommons being selectable in the SPECIFIC_FISH competition](https://media.discordapp.net/attachments/723194663519125618/883376510554226828/unknown.png)


`number-needed:`

This is the number of fish needed to win in the SPECIFIC_FISH competition type. By setting this to a value greater than 1, /emf top will be available to your players, and it will look like the MOST_FISH competition type - however players' scores only increase when they catch the selected fish. If this number isn't present or is set to 0, the plugin will check for a value in the general section, if the general section lacks the `number-needed` value or it's set to 0, the plugin will default to 1: disabling the leaderboard.

![a SPECIFIC_FISH competition starting which requires multiple of the selected fish](https://media.discordapp.net/attachments/723194663519125618/883378196085616691/unknown.png)

***

## General Settings

    general:

`minimum-players:`

This is used if your competition doesn't have its own value of this set, for information on this value: go here.

`broadcast-only-rods:`

By setting this to **true**, only players who are holding a rod will receive messages about the ongoing competition. This applies if they're using the main-hand or off-hand. This value could be used if you're running lots of competitions that last for a long time and want to avoid sending messages to your players who aren't competing in the competition.

`bossbar-colour, bossbar-prefix, allowed-rarities, number-needed`

These are used if your competition doesn't have its own value of these set, for information on these value: check above.

***

## Leaderboard Settings

    leaderboard:

<u>`position-colours:`</u>

These are used in the {pos_colour} variable, each string in the list is each position in the leaderboard, you need to set the same number of colours as the number of positions in the leaderboard you've set to show (messages.yml line:67)

![an example of position-colours](https://media.discordapp.net/attachments/723194663519125618/883384648120545310/unknown.png)

You can see that this sets the player name and fish number to the colour set in the `position-colours` value. You can modify where the position colour appears by changing the value in (messages.yml, line:63). You might want something different to appear for the LARGEST_FISH competition type and the MOST_FISH/SPECIFIC_FISH competition type, so you should see that you can change the values for both of them.

![rainbow leaderboard set from the yml code above](https://media.discordapp.net/attachments/723194663519125618/883385964809035796/unknown.png)

***

## Reward Settings

    rewards:

You can set as many positions to receive rewards as you want. Each position takes a list, so giving the top 3 rewards some diamonds would look like the below image. For more information on the types of rewards you can give to players, there's a whole wiki page for giving rewards to players and types of rewards: (coming soon).

![rewards set](https://media.discordapp.net/attachments/723194663519125618/883389012902035506/unknown.png)