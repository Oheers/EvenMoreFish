package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @deprecated Use {@link Message} instead. This class will be removed in EMF 1.8
 */
@Deprecated
public class OldMessage {

    // msg is the string got from the messages.yml file

    String msg;
    Player receiver;

    // the list of variables loaded
    List<Map.Entry<String, String>> variableMap = new ArrayList<>();

    public OldMessage() {

    }

    public OldMessage setMSG(String msg) {
        this.msg = msg;
        return this;
    }

    public OldMessage setReceiver(Player receiver) {
        this.receiver = receiver;
        return this;
    }

    public OldMessage setPlayer(String player) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{player}", player));
        return this;
    }

    public OldMessage setLength(String length) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{length}", length));
        return this;
    }

    public OldMessage setRarity(String rarity) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{rarity}", rarity));
        return this;
    }

    public OldMessage setPosition(String position) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{position}", position));
        return this;
    }

    public OldMessage setAmount(String amount) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{amount}", amount));
        return this;
    }

    public OldMessage setEffect(String effect) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{effect}", effect));
        return this;
    }

    public OldMessage setAmplifier(String amplifier) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{amplifier}", amplifier));
        return this;
    }

    public OldMessage setTime(String time) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{time}", time));
        return this;
    }

    public OldMessage setItem(String item) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{item}", item));
        return this;
    }

    public OldMessage setName(String name) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{name}", name));
        return this;
    }

    public OldMessage setBait(String bait) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{bait}", bait));
        return this;
    }

    public OldMessage setCurrBaits(String currBaits) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{current_baits}", currBaits));
        return this;
    }

    public OldMessage setMaxBaits(String maxBaits) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{max_baits}", maxBaits));
        return this;
    }

    public OldMessage setBaitTheme(String theme) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{bait_theme}", theme));
        return this;
    }

    public String toString() {

        if (EvenMoreFish.getInstance().isUsingPAPI()) {
            if (receiver != null) {
                msg = PlaceholderAPI.setPlaceholders(receiver, msg);
            }
        }

        for (Map.Entry<String, String> replacement : variableMap) {
            msg = msg.replace(replacement.getKey(), replacement.getValue());
        }

        return FishUtils.translateColorCodes(msg);

    }
}