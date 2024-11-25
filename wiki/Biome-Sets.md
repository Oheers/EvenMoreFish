# Outdated, use the [Biome Set Requirement](https://github.com/Oheers/EvenMoreFish/wiki/Requirements#biome-sets)
It is possible to re-use create and re-use biome groups for fish requirements.

Create a group by modifying your fish.yml with this info:
```yaml
biome-groups:
  ocean_beach: &ocean_beach
    ? OCEAN
    ? BEACH
```

And then in a particular fish, reuse the group you just created "ocean_beach"
```yaml
cod:
  requirements:
    biome:
       <<: *ocean_beach
```

You can even extend this further by adding more info to the group later, i.e:
```yaml
white_cod:
  requirements:
    biome:
       <<: *ocean_beach
       ? JUNGLE
```

