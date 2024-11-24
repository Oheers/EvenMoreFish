package com.oheers.fish.competition;


public interface CompetitionStrategy {
    boolean begin(Competition competition);
    void applyLeaderboard();
    void applyConsoleLeaderboard();
    void sendPlayerLeaderboard();

}