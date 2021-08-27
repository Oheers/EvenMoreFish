package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.CompetitionType;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Message {

    // msg is the string got from the messages.yml file
    // all the others are values that don't need to be set.

    String msg, player, colour, length, fishCaught, rarity, cmd, cmdDescription, position, amount, sellprice, effect, amplifier, time, item, posColour, type;
    Player receiver;

    public Message() {

    }

    public Message setMSG(String msg) {
        this.msg = msg;
        return this;
    }

    public Message setPlayer(String player) {
        this.player = player;
        return this;
    }

    public Message setColour(String colour) {
        this.colour = colour;
        return this;
    }

    public Message setLength(String length) {
        this.length = length;
        return this;
    }

    public Message setFishCaught(String fishCaught) {
        this.fishCaught = fishCaught;
        return this;
    }

    public Message setRarity(String rarity) {
        this.rarity = rarity;
        return this;
    }

    public Message setCMD(String cmd) {
        this.cmd = cmd;
        return this;
    }
    public Message setDesc(String description) {
        this.cmdDescription = description;
        return this;
    }

    public Message setPosition(String position) {
        this.position = position;
        return this;
    }

    public Message setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public Message setSellPrice(String sellPrice) {
        this.sellprice = sellPrice;
        return this;
    }

    public Message setReceiver(Player receiver) {
        this.receiver = receiver;
        return this;
    }

    public Message setEffect(String effect) {
        this.effect = effect;
        return this;
    }

    public Message setAmplifier(String amplifier) {
        this.amplifier = amplifier;
        return this;
    }

    public Message setTime(String time) {
        this.time = time;
        return this;
    }

    public Message setItem(String item) {
        this.item = item;
        return this;
    }

    public Message setPositionColour(String colour) {
        this.posColour = colour;
        return this;
    }

    public Message setType(CompetitionType type) {
        switch (type) {
            case MOST_FISH: this.type = EvenMoreFish.msgs.getTypeVariable("most"); break;
            case SPECIFIC_FISH: this.type = EvenMoreFish.msgs.getTypeVariable("specific"); break;
            default: this.type = EvenMoreFish.msgs.getTypeVariable("largest"); break;
        }
        return this;
    }

    public String toString() {

        if (type != null) {
            msg = msg.replace("{type}", type);
        }

        if (player != null) {
            msg = msg.replace("{player}", player);
        }

        if (length != null) {
            DecimalFormat df = new DecimalFormat("###,###.#");
            String formatted = df.format(Double.parseDouble(length));
            msg = msg.replace("{length}", colour + formatted);
        }

        if (fishCaught != null) {
            msg = msg.replace("{fish}", colour + "&l" + fishCaught);
        }

        if (rarity != null) {
            msg = msg.replace("{rarity}", colour + "&l" + rarity);
        }

        if (cmd != null) {
            msg = msg.replace("{command}", cmd);
        }

        if (cmdDescription != null) {
            msg = msg.replace("{description}", cmdDescription);
        }

        if (position != null) {
            msg = msg.replace("{position}", position);
        }

        if (amount != null) {
            msg = msg.replace("{amount}", amount);
        }

        if (sellprice != null) {
            msg = msg.replace("{sell-price}", NumberFormat.getInstance(Locale.US).format(new BigDecimal(sellprice)));
        }

        if (effect != null) {
            msg = msg.replace("{effect}", effect);
        }

        if (amplifier != null) {
            msg = msg.replace("{amplifier}", amplifier);
        }

        if (time != null) {
            msg = msg.replace("{time}", time);
        }

        if (item != null) {
            msg = msg.replace("{item}", item);
        }

        if (posColour != null) {
            msg = msg.replace("{pos_colour}", posColour);
        }

        if (EvenMoreFish.papi) {
            if (receiver != null) {
                msg = PlaceholderAPI.setPlaceholders(receiver, msg);
            }

        }

        return FishUtils.translateHexColorCodes(msg);

    }
}