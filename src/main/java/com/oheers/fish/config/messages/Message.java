package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.CompetitionType;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

public class Message {

    // msg is the string got from the messages.yml file

    String msg;
    Player receiver;

    // the list of variables loaded
    List<Map.Entry<String, String>> variableMap = new ArrayList<>();

    public Message() {

    }

    public Message setMSG(String msg) {
        this.msg = msg;
        return this;
    }

    public Message setReceiver(Player receiver) {
        this.receiver = receiver;
        return this;
    }

    public Message setPlayer(String player) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{player}", player));
        return this;
    }

    public Message setLength(String length) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{length}", length));
        return this;
    }

    public Message setFishCaught(String fishCaught) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{fish}", fishCaught));
        return this;
    }

    public Message setRarity(String rarity) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{rarity}", rarity));
        return this;
    }

    public Message setPosition(String position) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{position}", position));
        return this;
    }

    public Message setAmount(String amount) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{amount}", amount));
        return this;
    }

    public Message setSellPrice(String sellPrice) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{sell-price}", NumberFormat.getInstance(Locale.US).format(new BigDecimal(sellPrice))));
        return this;
    }

    public Message setEffect(String effect) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{effect}", effect));
        return this;
    }

    public Message setAmplifier(String amplifier) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{amplifier}", amplifier));
        return this;
    }

    public Message setTime(String time) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{time}", time));
        return this;
    }

    public Message setTimeRaw(String timeRaw) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{time_raw}", timeRaw));
        return this;
    }

    public Message setTimeFormatted(String timeFormatted) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{time_formatted}", timeFormatted));
        return this;
    }

    public Message setItem(String item) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{item}", item));
        return this;
    }

    public Message setPositionColour(String colour) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{pos_colour}", colour));
        return this;
    }

    public Message setRarityColour(String colour) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{rarity_colour}", colour));
        return this;
    }

    public Message setDay(String day) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{day}", day));
        return this;
    }

    public Message setName(String name) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{name}", name));
        return this;
    }

    // The below 4 are unused, to be re-used with the introduction of fish logs.

    public Message setFirstCaught(String firstCaught) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{first_caught}", firstCaught));
        return this;
    }

    public Message setLargestSize(String largestSize) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{largest_size}", largestSize));
        return this;
    }

    public Message setNumCaught(String numCaught) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{num_caught}", numCaught));
        return this;
    }

    public Message setTimeRemaining(String timeRemaining) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{time_remaining}", timeRemaining));
        return this;
    }

    public Message setBait(String bait) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{bait}", bait));
        return this;
    }

    public Message setCurrBaits(String currBaits) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{current_baits}", currBaits));
        return this;
    }

    public Message setMaxBaits(String maxBaits) {
        variableMap.add(new AbstractMap.SimpleEntry<>("{max_baits}", maxBaits));
        return this;
    }

    public Message setType(CompetitionType type) {
        switch (type) {
            case MOST_FISH: variableMap.add(new AbstractMap.SimpleEntry<>("{type}", EvenMoreFish.msgs.getTypeVariable("most"))); break;
            case SPECIFIC_FISH: variableMap.add(new AbstractMap.SimpleEntry<>("{type}", EvenMoreFish.msgs.getTypeVariable("specific"))); break;
            default: variableMap.add(new AbstractMap.SimpleEntry<>("{type}", EvenMoreFish.msgs.getTypeVariable("largest"))); break;
        }
        return this;
    }

    public String toString() {

        if (EvenMoreFish.papi) {
            if (receiver != null) {
                msg = PlaceholderAPI.setPlaceholders(receiver, msg);
            }
        }

        for (Map.Entry<String, String> replacement : variableMap) {
            msg = msg.replace(replacement.getKey(), replacement.getValue());
        }

        return FishUtils.translateHexColorCodes(msg);

    }
}