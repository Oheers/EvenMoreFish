package com.oheers.fish.config.messages;

import org.bukkit.ChatColor;

import java.text.DecimalFormat;

public class Message {

    // msg is the string got from the messages.yml file
    // all the others are values that don't need to be set.

    String msg, player, colour, length, fishCaught, rarity, cmd, cmdDescription, position;

    public Message() {}

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

    public String toString() {

        if (player != null) {
            msg = msg.replace("{player}", player);
        }

        if (length != null) {
            DecimalFormat df = new DecimalFormat("###,###.#");
            String formatted = df.format(Double.parseDouble(length)) + "cm";
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

        return ChatColor.translateAlternateColorCodes('&', msg);

    }
}
