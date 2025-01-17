package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

public class Messages extends ConfigBase {

    private static Messages instance = null;

    public Messages() {
        super("messages.yml", "locales/" + "messages_" + MainConfig.getInstance().getLocale() + ".yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }

    public static Messages getInstance() {
        return instance;
    }

    public String getSTDPrefix() {
        AbstractMessage message = EvenMoreFish.getAdapter().createMessage("");
        message.prependMessage(PrefixType.DEFAULT.getPrefix());
        message.appendString("&r");
        return message.getLegacyMessage();
    }

    @Override
    public UpdaterSettings getUpdaterSettings() {
        UpdaterSettings.Builder builder = UpdaterSettings.builder(super.getUpdaterSettings());

        // Bossbar config relocations - config version 2
        builder.addCustomLogic("2", yamlDocument -> {
            String hourColor = yamlDocument.getString("bossbar.hour-color", "&f");
            String hour = yamlDocument.getString("bossbar.hour", "h");
            yamlDocument.set("bossbar.hour", hourColor + "{hour}" + hour);
            yamlDocument.remove("bossbar.hour-color");

            String minuteColor = yamlDocument.getString("bossbar.minute-color", "&f");
            String minute = yamlDocument.getString("bossbar.minute", "m");
            yamlDocument.set("bossbar.minute", minuteColor + "{minute}" + minute);
            yamlDocument.remove("bossbar.minute-color");

            String secondColor = yamlDocument.getString("bossbar.second-color", "&f");
            String second = yamlDocument.getString("bossbar.second", "s");
            yamlDocument.set("bossbar.second", secondColor + "{second}" + second);
            yamlDocument.remove("bossbar.second-color");
        });

        // Prefix config relocations - config version 3
        builder.addCustomLogic("3", yamlDocument -> {
            String prefix = yamlDocument.getString("prefix");

            String oldRegular = yamlDocument.getString("prefix-regular");
            yamlDocument.set("prefix-regular", oldRegular + prefix);

            String oldAdmin = yamlDocument.getString("prefix-admin");
            yamlDocument.set("prefix-admin", oldAdmin + prefix);

            String oldError = yamlDocument.getString("prefix-error");
            yamlDocument.set("prefix-error", oldError + prefix);

            yamlDocument.remove("prefix");
        });

        return builder.build();
    }

}
