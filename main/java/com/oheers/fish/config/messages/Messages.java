package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class Messages {

    public static String fishCaught = EvenMoreFish.messageFile.getConfig().getString("fish-caught");

    public static String noPermission = EvenMoreFish.messageFile.getConfig().getString("no-permission");

    /*
     * @param player the name of the player.
     * @param colour the colour of the fish (rarity)
     * @param length the length of the player (adds commas and units) e.g. 4021 to 4,021cm
     * @param fishCaught the name of the caught fish
     *
     * I am aware this is probably a shocking way of doing things but it does what I need
     * if this is still existing in post-release and you think you have a better way of doing this, please make a pull request.
     */

    public static String renderMessage(String s, String player, String colour, String length, String fishCaught, String rarity) {

        if (player != null) {
            s = s.replace("{player}", player);
        }

        if (length != null) {
            DecimalFormat df = new DecimalFormat("###,###.#");
            String formatted = df.format(Double.parseDouble(length)) + "cm";
            s = s.replace("{length}", colour + formatted);
        }

        if (fishCaught != null) {
            s = s.replace("{fish}", colour + "&l" + fishCaught);
        }

        if (rarity != null) {
            s = s.replace("{rarity}", colour + "&l" + rarity);
        }

        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
