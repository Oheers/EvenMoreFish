package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Message {

    // msg is the string got from the messages.yml file
    // all the others are values that don't need to be set.

    String msg, player, colour, length, fishCaught, rarity, cmd, cmdDescription, position, amount, sellprice;
    Player p;

    public Message(Player p) {
        this.p = p;
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

    public String toString() {

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

        if (EvenMoreFish.papi) {
            if (p != null) {
                msg = PlaceholderAPI.setPlaceholders(p, msg);
            }

        }

        return FishUtils.translateHexColorCodes(msg);

    }
}
