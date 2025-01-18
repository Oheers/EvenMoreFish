package com.oheers.fish.commands.arguments;

import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

/**
 * Should only be used at any point AFTER a RarityArgument. It will not work otherwise.
 */
public class FishArgument {

    public static Argument<Fish> create() {
        return new CustomArgument<>(new StringArgument("fish"), info -> {
            Rarity rarity = info.previousArgs().getUnchecked("rarity");
            if (rarity == null) {
                throw CustomArgument.CustomArgumentException.fromString("Could not find a previous RarityArgument!");
            }
            Fish fish = rarity.getFish(info.input());
            if (fish == null) {
                fish = rarity.getFish(info.input().replace("_", " "));
            }
            if (fish == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                        new CustomArgument.MessageBuilder("Unknown fish: ").appendArgInput()
                );
            }
            return fish;
        }).replaceSuggestions(ArgumentHelper.getAsyncSuggestions(
                info -> {
                    Rarity rarity = info.previousArgs().getUnchecked("rarity");
                    if (rarity == null) {
                        return new String[0];
                    }
                    return rarity.getFishList().stream().map(fish -> fish.getName().replace(" ", "_")).toArray(String[]::new);
                }
        ));
    }

}
