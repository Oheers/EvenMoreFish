To use, change the identifier to the one you want using the table below:
```yaml
rewards:
   1:
     - "MONEY:5000"
     - "MESSAGE:&eCongratulations!"
   2:
     - "MONEY:2000"
```

## Default Types
| Reward Type | Identifier | Description                                      | Available placeholders                     |
|-------------|------------|--------------------------------------------------|--------------------------------------------|
| Command     | COMMAND    | Run a command.                                   | `{player}`, `{x}`, `{y}`, `{z}`, `{world}` |
| Effect      | EFFECT     | Adds a potion effect.                            | -                                          |
| Health      | HEALTH     | Adds health.                                     | -                                          |
| Hunger      | HUNGER     | Sets hunger level                                | -                                          |
| Items       | ITEM       | Gives an item (doesn't support addons - for now) | -                                          |
| Message     | MESSAGE    | Sends a message                                  | -                                          |
| Exp         | EXP        | Gives experience points.                         | -                                          |

## External Plugin Types
_These will only load if the relevant external plugin is installed._
| Reward Type                  | Plugin                    | Identifier      | Description            |
|------------------------------|---------------------------|-----------------|------------------------|
| PlayerPoints                 | PlayerPoints              | PLAYER_POINTS   | Gives player points.   |
| GriefPrevention Claim Blocks | GriefPrevention           | GP_CLAIM_BLOCKS | Gives GP claim blocks. |
| AuraSkills XP                | AuraSkills                | AURASKILLS_XP   | Gives AuraSkills xp.   |
| McMMO XP                     | McMMO                     | MCMMO_XP        | Gives McMMO xp.        |
| Permission                   | Vault + Permission Plugin | PERMISSION      | Gives a permission.    |
| Money                        | Vault + Economy Plugin    | MONEY           | Gives money.           |