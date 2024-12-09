package com.oheers.fish.database.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CompetitionReport {

    private final String competitionConfigID;
    private final String winnerFish;
    private final UUID winnerUUID;
    private final List<UUID> contestants = new ArrayList<>();

    private final float winnerScore;

    public CompetitionReport(@NotNull final String competitionConfigID, @NotNull final String winnerUUIDString,
                             @NotNull final String winnerFish, final float winnerScore, @NotNull final String contestants) {
        this.competitionConfigID = competitionConfigID;
        this.winnerUUID = UUID.fromString(winnerUUIDString);
        this.winnerFish = winnerFish;
        this.winnerScore = winnerScore;
        for (String contestant : contestants.split(",")) {
            this.contestants.add(UUID.fromString(contestant));
        }
    }

    public String getCompetitionConfigID() {
        return competitionConfigID;
    }

    public String getWinnerFish() {
        return winnerFish;
    }

    public UUID getWinnerUUID() {
        return winnerUUID;
    }

    public List<UUID> getContestants() {
        return contestants;
    }

    public float getWinnerScore() {
        return winnerScore;
    }
}
