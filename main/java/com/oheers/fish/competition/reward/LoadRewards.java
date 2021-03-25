package com.oheers.fish.competition.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.sun.tools.javac.Main;

import java.util.ArrayList;
import java.util.List;

public class LoadRewards {

    public static void load() {
        for (String position : MainConfig.plugin.getConfig().getConfigurationSection("competitions.winnings").getKeys(false)) {
            List<String> rewards = MainConfig.plugin.getConfig().getStringList("competitions.winnings." + position);
            List<Reward> rewardList = new ArrayList<>();

            for (String reward : rewards) {
                rewardList.add(new Reward((reward)));
            }

            EvenMoreFish.rewards.put(Integer.parseInt(position), rewardList);
        }
    }
}
