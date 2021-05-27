package com.oheers.fish.competition.reward;

import com.oheers.fish.EvenMoreFish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoadRewards {

    public static HashMap<Integer, List<Reward>> load() {
        HashMap<Integer, List<Reward>> rewardlist = new HashMap<>();
        for (String position : EvenMoreFish.mainConfig.getTotalRewards()) {
            List<String> rewards = EvenMoreFish.mainConfig.getPositionRewards(position);
            List<Reward> rewardList = new ArrayList<>();

            for (String reward : rewards) {
                rewardList.add(new Reward((reward)));
            }

            rewardlist.put(Integer.parseInt(position), rewardList);
        }
        return rewardlist;
    }
}
