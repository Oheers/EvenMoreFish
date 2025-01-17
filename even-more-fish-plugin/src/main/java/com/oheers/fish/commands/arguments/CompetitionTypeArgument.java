package com.oheers.fish.commands.arguments;

import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

import java.util.Arrays;

public class CompetitionTypeArgument {

    public static Argument<CompetitionType> create() {
        return new CustomArgument<>(new StringArgument("competitionType"), info -> {
            CompetitionType type = CompetitionType.getType(info.input());
            if (type == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                        new CustomArgument.MessageBuilder("Unknown competition type: ").appendArgInput()
                );
            }
            return type;
        }).replaceSuggestions(ArgumentSuggestions.strings(
                Arrays.stream(CompetitionType.values()).map(CompetitionType::toString).toArray(String[]::new)
        ));
    }

}
