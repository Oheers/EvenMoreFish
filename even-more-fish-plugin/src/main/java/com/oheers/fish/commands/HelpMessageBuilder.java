package com.oheers.fish.commands;

import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HelpMessageBuilder {

    private final Map<String, String> usages;

    private HelpMessageBuilder(@NotNull Map<String, String> usages) {
        this.usages = usages;
    }

    /**
     * Creates a HelpMessageBuilder instance
     */
    public static HelpMessageBuilder create(@NotNull Map<String, String> usages) {
        return new HelpMessageBuilder(usages);
    }

    /**
     * Creates the final message.
     * @return The created help message
     */
    public AbstractMessage buildMessage() {
        final AbstractMessage message = ConfigMessage.HELP_GENERAL_TITLE.getMessage();
        usages.forEach((key, value) -> {
            AbstractMessage usage = ConfigMessage.HELP_FORMAT.getMessage();
            usage.setVariable("{command}", correctCommand(key));
            usage.setVariable("{description}", value);
            message.appendString("\n");
            message.appendString(usage.getLegacyMessage());
        });
        return message;
    }

    /**
     * Adds "/[commandname] " to the start of the provided usage.
     */
    private String correctCommand(@NotNull String key) {
        return "/" + MainConfig.getInstance().getMainCommandName() + " " + key;
    }

    /**
     * Sends the final message to the provided sender.
     * @param sender The sender to send the message to.
     */
    public void sendMessage(@NotNull CommandSender sender) {
        buildMessage().send(sender);
    }


}
