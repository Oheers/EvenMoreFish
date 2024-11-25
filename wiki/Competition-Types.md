## What are they?
There are 5 different competition types in EvenMoreFish for your players to experience, and include: MOST_FISH, LARGEST_FISH and SPECIFIC_FISH. Each competition you choose must define a competition type for it to work, however if you want to start a competition via commands, "**/emf admin competition start <duration>**" it will default to the LARGEST_FISH competition type. An argument after duration can be used to start a chosen competition type, as is seen in the screenshot below.

![starting a fishing competition by command](https://media.discordapp.net/attachments/723194663519125618/883038254042083408/unknown.png)

## LARGEST_FISH
As the title would suggest, the player with the largest fish caught will win the competition, and the player with the second largest fish will come second (and so on), by using **/emf top** your players will be able to view the current leaderboard - requiring they have the `emf.top` permission node.

![a leaderboard for the LARGEST_FISH competition type](https://media.discordapp.net/attachments/723194663519125618/883043716686827550/unknown.png)

## MOST_FISH
In this competition, your players will be ranked in the leaderboard by the number of fish they catch. This type is likely to be biased by the "Lure" enchant which naturally causes players to catch more fish - however your players will also be very close to each other in the competition and won't be able to just fish 1 legendary and walk away, leading to (hopefully) a very intense competition, you may want to set the `new-first` message in `messages.yml` to be a bossbar message, otherwise it could lead to spam in the chat as the competition begins and players are all catching fish.

![leaderboard for the MOST_FISH competition type](https://media.discordapp.net/attachments/723194663519125618/883046296615788565/unknown.png)

## SPECIFIC_FISH
This competition type selects a fish at random from the list of `allowed-rarities` you give it, if it's started by command, the allowed rarities are chosen from the value in the general section. By default, only 1 of the selected fish is needed to win the competition, therefore /emf top is disabled for this event however you can set the number of the fish needed to a different value to increase the difficulty, allowing the leaderboard to be enabled in a similar style to the MOST_FISH competition type. Please keep in mind that setting this to multiple fish needed of a rare rarity could result in a competition rarely getting winners, as the competition still ends after the set timer regardless of whether the specific fish was caught or not.

![the SPECIFIC_FISH competition type beginning](https://media.discordapp.net/attachments/723194663519125618/883063415453655050/unknown.png)

## SPECIFIC_RARITY
This competition type selects a rarity at random from the list of `allowed-rarities` you give it, if it's started by command, the allowed rarities are chosen from the value in the general section. By default, only 1 fish with its rarity as the selected rarity is needed to win the competition, therefore /emf top is disabled for this event however you can set the number of the fish needed to a different value to increase the difficulty, allowing the leaderboard to be enabled in a similar style to the MOST_FISH competition type. Please keep in mind that setting this to contain rare rarities could result in a competition rarely getting winners, as the competition still ends after the set timer regardless of whether the specific rarity was caughtor not.

![the SPECIFIC_RARITY competition type beginning](https://media.discordapp.net/attachments/723194663519125618/981585087437086770/unknown.png)

## LARGEST_TOTAL
When a player catches a fish, their score is increased by the size of their fish, allowing players to gradually increase their score cumulatively. At the end of the competition, the player with the highest score will win, similar to all other competitions - it's similar to the more_fish competition in that the %emf_competition_place_fish_*% placeholder isn't able to be used too.

![the leaderboard from the LARGEST_TOTAL competition](https://media.discordapp.net/attachments/723194663519125618/981639014601859132/unknown.png)