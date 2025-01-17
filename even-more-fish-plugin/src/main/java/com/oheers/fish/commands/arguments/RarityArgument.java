package com.oheers.fish.commands.arguments;

import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.jetbrains.annotations.NotNull;

public class RarityArgument {

    public static Argument<Rarity> create() {
        return new CustomArgument<>(new StringArgument("rarity"), info -> {
            Rarity rarity = FishManager.getInstance().getRarity(info.input());
            if (rarity == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                        new CustomArgument.MessageBuilder("Unknown rarity: ").appendArgInput()
                );
            } else {
                return rarity;
            }
        }).replaceSuggestions(ArgumentHelper.getAsyncSuggestions(
                info -> FishManager.getInstance().getRarityMap().keySet().toArray(String[]::new)
        ));
    }

}
