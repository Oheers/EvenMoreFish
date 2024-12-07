## The files
As of EvenMoreFish 2.0, all competition config files are located in `plugins/EvenMoreFish/competitions`.

## Creating Competitions
To create a new competition, you need to create a new yml file in the competitions folder.

The following configs are required in each competition config file:
- `id` - Allows the plugin to identify this competition internally, mainly used in the Database
- `type` - Controls the type of competition this is. You can see a list of valid types [here](https://github.com/Oheers/EvenMoreFish/wiki/Competition-Types)
- `duration` - Controls how long this competition should last for. This is measured in minutes.

All other configs are optional, however you will most likely want to schedule your competition for specific times in the day.

## Scheduling Competitions
There are two options for scheduling a competition, both will be explained below:

### Option 1, `times`:

This is the newer of the two options, and is as simple as writing down a list of times you want to run at:
```yaml
# This competition will run every 6 hours.
times:
  - 00:00
  - 06:00
  - 12:00
  - 18:00
```

Times can be paired with the `blacklisted-days` config to further customize your schedule:
```yaml
# Prevents the competition from running on these days.
blacklisted-days:
  - MONDAY
  - WEDNESDAY
  - FRIDAY
  - SUNDAY
```

### Option 2, `days`:

This config allows full control over days and times all in one:
```yaml
# This competition will run on:
# Saturday at 3am and 3pm
# Sunday at 4am and 2pm
days:
  Saturday:
    - "03:00"
    - "15:00"
  Sunday:
    - "04:00"
    - "14:00"
```

These two options cannot be mixed in the same file. If both are present, `days` is prioritised.

## Disabling Competitions
To disable a competition, you have two choices:
- Set `disabled` to true inside the file and reload.
- Rename the file to start with an underscore.

Doing either of these will prevent the competition from being started.

## Example Config
An example config will always be available inside your competitions folder, and contains every possible config option.
This file will reset every time the plugin loads, meaning it will always be up to date.

You can view this example file [here](https://github.com/Oheers/EvenMoreFish/blob/master/even-more-fish-plugin/src/main/resources/competitions/_example.yml)