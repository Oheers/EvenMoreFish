package com.oheers.fish.commands;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CommandUtil {
    private CommandUtil(){
        throw new UnsupportedOperationException();
    }
    public static String formGeneralHelp(CommandSender user) {

        //return new Message(ConfigMessage.HELP_GENERAL).getRawMessage(true, false);

        StringBuilder out = new StringBuilder();
        List<String> commands = Arrays.asList(new Message(ConfigMessage.HELP_GENERAL).getRawMessage(true, false).split("\n"));

        String escape = "\n";
        if (EvenMoreFish.getInstance().getPermission() != null && user != null) {
            for (int i = 0; i < commands.size(); i++) {
                if (i == commands.size() - 1) escape = "";
                if (commands.get(i).contains("/emf admin")) {
                    if (EvenMoreFish.getInstance().getPermission().has(user, AdminPerms.ADMIN)) out.append(commands.get(i)).append(escape);
                } else if (commands.get(i).contains("/emf top")) {
                    if (EvenMoreFish.getInstance().getPermission().has(user, UserPerms.TOP)) out.append(commands.get(i)).append(escape);
                } else if (commands.get(i).contains("/emf shop")) {
                    if (EvenMoreFish.getInstance().getPermission().has(user, UserPerms.SHOP)) out.append(commands.get(i)).append(escape);
                } else if (commands.get(i).contains("/emf toggle")) {
                    if (EvenMoreFish.getInstance().getPermission().has(user, UserPerms.TOGGLE)) out.append(commands.get(i)).append(escape);
                } else out.append(commands.get(i)).append(escape);
            }
        } else {
            for (int i = 0; i < commands.size(); i++) {
                if (i == commands.size() - 1) escape = "";
                out.append(FishUtils.translateHexColorCodes(Messages.getInstance().getSTDPrefix() + commands.get(i) + escape));
            }
        }

        return out.toString();

    }

    public static String wrapped(final String string) {
        return "\"" + string + "\"";
    }
}
