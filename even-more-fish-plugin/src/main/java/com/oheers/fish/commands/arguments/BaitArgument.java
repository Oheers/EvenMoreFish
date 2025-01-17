package com.oheers.fish.commands.arguments;

import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitManager;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class BaitArgument {

    public static Argument<Bait> create() {
        return new CustomArgument<>(new StringArgument("bait"), info -> {
            Bait bait = BaitManager.getInstance().getBait(info.input());
            if (bait == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                        new CustomArgument.MessageBuilder("Unknown bait: ").appendArgInput()
                );
            }
            return bait;
        }).replaceSuggestions(ArgumentHelper.getAsyncSuggestions(
                info -> BaitManager.getInstance().getBaitMap().keySet().toArray(String[]::new)
        ));
    }

}
