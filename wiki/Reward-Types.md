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
| Reward Type | Example                             | Description                                      | Available placeholders                     |
|-------------|-------------------------------------|--------------------------------------------------|--------------------------------------------|
| Command     | `COMMAND:msg {player} Reward Type!` | Run a command.                                   | `{player}`, `{x}`, `{y}`, `{z}`, `{world}` |
| Effect      | `EFFECT:POISON,1,2`                 | Adds a potion effect.                            | -                                          |
| Health      | `HEALTH:5`                          | Adds health.                                     | -                                          |
| Hunger      | `HUNGER:20`                         | Sets hunger level                                | -                                          |
| Items       | `ITEM:cobblestone,1`                | Gives an item (doesn't support addons - for now) | -                                          |
| Message     | `MESSAGE:Reward Type!`              | Sends a message                                  | -                                          |
| Exp         | `EXP:5000`                          | Gives experience points.                         | -                                          |

## External Plugin Types
_These will only load if the relevant external plugin is installed._
| Reward Type                  | Plugin                    | Example                      | Description            |
|------------------------------|---------------------------|------------------------------|------------------------|
| PlayerPoints                 | PlayerPoints              | `PlayerPoints:10`            | Gives player points.   |
| GriefPrevention Claim Blocks | GriefPrevention           | `GP_CLAIM_BLOCKS:5`          | Gives GP claim blocks. |
| AuraSkills XP                | AuraSkills                | `AURASKILLS_XP:Archery,10`   | Gives AuraSkills xp.   |
| McMMO XP                     | McMMO                     | `MCMMO_XP:Woodcutting,15`    | Gives McMMO xp.        |
| Permission                   | Vault + Permission Plugin | `PERMISSION:emf.example`     | Gives a permission.    |
| Money                        | Vault + Economy Plugin    | `MONEY:1000`                 | Gives money.           |