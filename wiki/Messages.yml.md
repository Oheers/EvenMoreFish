## The file
This is the file for modifying messages sent to the players from the plugin: if you're running the server locally or viewing the files on a panel through FTP/SFTP, you can use a text-editor such as [Notepad++](https://notepad-plus-plus.org/). At some point in the very distant future I plan to add in a way of modifying this file using a web-editor like LuckPerms uses, but right now (and probably for quite a while) it's just the basic text editor.

## The Wiki
This file has every value explained already so unlike the [Competition Wiki](https://github.com/Oheers/EvenMoreFish/wiki/Competition-Configs) and [Rarity Wiki](https://github.com/Oheers/EvenMoreFish/wiki/Rarities), not every value will be explained but a copy of messages_en.yml can be found on the Spigot page in the "Technical Stuff" section.

***

## Tags
It's possible to add "tags" to the messages, these cause the message to be formatted differently or to act differently. Right now (as of 1.7.3) there are only two - `-s` and `[noPrefix]`, there will be more in the future. It's worth noting that not all tags work for all messages due to issues with formatting, consistency or it just doesn't make sense to have one (i.e. a `-s` tag for the /emf help page wouldn't be very useful). All colours are supported by the &_ format or &#______ for hex codes. 

#### -s
This variable simply causes the message to not be sent. You might want to do this if you don't want players to see when someone overtakes them in the competition leaderboard (previously you had to delete the config value to do this).

#### [noPrefix] 
This stops the [EvenMoreFish] logo (or your server's equivalent) from displaying before the message. For lists like /emf help, you have to add one for each line of the list.

***

## Use Table
This shows the table for which tabs can be used in each message. The `id` column represents the tag given before each message in the file, (`new-first: &r{player} is now #1`: `new-first` is the id in this case. You may notice some are separated by full stops, this simply means that it is in a sub-section, for example `bossbar.hour` points to the "`hour: h` value in the `bossbar:` section. 
➖ (Not applicable: there isn't a prefix there anyway).
 
|             Message ID              | Tag: `-s` | Tag: `[noPrefix]` |
|:-----------------------------------:|:---------:|:-----------------:|
|    `admin.cannot-run-on-console`    |     ❌     |        ✔️         |
|      `admin.given-player-bait`      |    ✔️     |        ✔️         |
|      `admin.given-player-fish`      |    ✔️     |        ✔️         |
|       `admin.open-fish-shop`        |    ✔️     |        ✔️         |
|      `admin.no-bait-specified`      |     ❌     |        ✔️         |
|     `admin.must-be-holding-rod`     |     ❌     |         ❌         |
|     `admin.number-format-error`     |     ❌     |        ✔️         |
|     `admin.number-range-error`      |     ❌     |        ✔️         |
|      `admin.player-not-found`       |     ❌     |        ✔️         |
|      `admin.update-available`       |     ❌     |         ❌         |
|      `admin.all-baits-cleared`      |    ✔️     |         ❌         |
|            `bait-catch`             |    ✔️     |         ➖         |
|             `bait-use`              |    ✔️     |         ❌         |
|       `bait-survival-limited`       |     ❌     |         ❌         |
|         `max-baits-reached`         |     ❌     |         ❌         |
|          `bossbar.second`           |    ✔️     |         ➖         |
|          `bossbar.minute`           |    ✔️     |         ➖         |
|           `bossbar.hour`            |    ✔️     |         ➖         |
|         `bossbar.remaining`         |    ✔️     |         ➖         |
| `admin.competition-already-running` |     ❌     |        ✔️         |
|            `contest-end`            |     ❌     |         ❌         |
|           `contest-join`            |    ✔️     |         ❌         |
|           `contest-start`           |     ❌     |         ❌         |
|     `competition-types.largest`     |    ✔️     |         ➖         |
|  `competition-types.largest-total`  |    ✔️     |         ➖         |
|      `competition-types.most`       |    ✔️     |         ➖         |
|    `competition-types.specific`     |    ✔️     |         ➖         |
| `competition-types.specific-rarity` |    ✔️     |         ➖         |
|           `single-winner`           |    ✔️     |        ✔️         |
|      `admin.economy-disabled`       |     ❌     |         ❌         |
|        `place-fish-blocked`         |    ✔️     |        ✔️         |
|            `fish-caught`            |    ✔️     |         ➖         |
|      `lengthless-fish-caught`       |    ✔️     |         ➖         |
|             `fish-lore`             |     ❌     |         ➖         |
|             `fish-sale`             |    ✔️     |        ✔️         |
|           `help-general`            |     ❌     |        ✔️         |
|            `help-admin`             |     ❌     |        ✔️         |
|         `help-competition`          |     ❌     |        ✔️         |
|  `admin.competition-type-invalid`   |     ❌     |         ❌         |
|     `leaderboard-largest-fish`      |     ❌     |        ✔️         |
|     `leaderboard-largest-total`     |     ❌     |        ✔️         |
|       `leaderboard-most-fish`       |     ❌     |        ✔️         |
|           `total-players`           |    ✔️     |        ✔️         |
|             `new-first`             |    ✔️     |        ✔️         |
|          `no-baits-on-rod`          |    ✔️     |        ✔️         |
|      `no-competition-running`       |     ❌     |         ❌         |
|             `no-record`             |    ✔️     |        ✔️         |
|           `no-permission`           |     ❌     |        ✔️         |
|            `no-winners`             |    ✔️     |        ✔️         |
|        `not-enough-players`         |    ✔️     |         ❌         |
|    `emf-competition-fish-format`    |    ✔️     |         ➖         |
|    `emf-lengthless-fish-format`     |    ✔️     |         ➖         |
|       `emf-most-fish-format`        |    ✔️     |         ➖         |
|      `no-competition-running`       |    ✔️     |         ➖         |
|        `no-player-in-place`         |    ✔️     |         ➖         |
|     `emf-size-during-most-fish`     |    ✔️     |         ➖         |
|        `emf-time-remaining`         |    ✔️     |         ➖         |
|           `admin.reload`            |     ❌     |         ❌         |
|            `time-alert`             |     ❌     |        ✔️         |
|             `toggle-on`             |     ❌     |        ✔️         |
|            `toggle-off`             |     ❌     |        ✔️         |
|          `worth-gui-name`           |     ❌     |         ➖         |
|     `confirm-sell-all-gui-name`     |     ❌     |         ➖         |
|         `confirm-gui-name`          |     ❌     |         ➖         |
|          `error-gui-name`           |     ❌     |         ➖         |
|          `error-gui-lore`           |     ❌     |         ➖         |
|      `error-sell-all-gui-name`      |     ❌     |         ➖         |
|           `sell-all-name`           |     ❌     |         ➖         |
|           `sell-all-lore`           |     ❌     |         ➖         |
|           `sell-gui-name`           |     ❌     |         ➖         |
|      `error-sell-all-gui-lore`      |     ❌     |         ➖         |
|           `sell-gui-lore`           |     ❌     |         ➖         |
