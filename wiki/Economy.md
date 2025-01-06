## How to use EMF's new economy system:
As of EvenMoreFish 2.0, the plugin is capable of supporting multiple economies at the same time.

Below is the default config as of December 4th 2024:
```
# Enable/Disable different economy types here.
# Optionally choose a multiplier for each type you have enabled.
economy:
  # Allows fish to pay out with your Vault economy plugin
  vault:
    enabled: false
    multiplier: 1.0
  # Allows fish to pay out with PlayerPoints
  playerpoints:
    enabled: false
    multiplier: 1.0
    display: "{amount} Player Point(s)"
  # Allows fish to pay out with claim blocks
  griefprevention:
    enabled: false
    multiplier: 1.0
    display: "{amount} Claim Block(s)"
```

In this config, each supported economy is listed with the following options:
- `enabled` - This allows you to enable or disable each type as you please. Changes to this can be reloaded with the `/emf admin reload` command (only if the required plugin was loaded during server startup)
- `multiplier` - This allows you to add a multiplier to this economy, so you could set this to (for example) 0.25, and a fish with a value 400 is now worth 100 of this currency.
- `display` - This allows you to translate how this economy shows up in chat, for example in the message when you sell a fish.