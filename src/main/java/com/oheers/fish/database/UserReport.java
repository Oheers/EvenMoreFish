package com.oheers.fish.database;

import java.util.UUID;

public class UserReport {

    private final int id;
    private String firstFish;
    private String recentFish;
    private String largestFish;
    private UUID uuid;
    private int numFishCaught;
    private int competitionsWon;
    private int competitionsJoined;

    private float largestLength;
    private float totalFishLength;
    
    private int fishSold;
    private double moneyEarned;

    public UserReport(final int id, final int numFishCaught, final int competitionsWon, final int competitionsJoined,
                      final String firstFish, final String recentFish, final String largestFish, final float totalFishLength,
                      final float largestLength, final String uuid) {
        this.id = id;
        this.numFishCaught = numFishCaught;
        this.competitionsWon = competitionsWon;
        this.competitionsJoined = competitionsJoined;
        this.firstFish = firstFish;
        this.recentFish = recentFish;
        this.largestFish = largestFish;
        this.totalFishLength = totalFishLength;
        this.largestLength = largestLength;
        this.uuid = UUID.fromString(uuid);
    }
    
    public UserReport(final int id, final int numFishCaught, final int competitionsWon, final int competitionsJoined,
                      final String firstFish, final String recentFish, final String largestFish, final float totalFishLength,
                      final float largestLength, final String uuid, final int fishSold, final double moneyEarned) {
        this.id = id;
        this.numFishCaught = numFishCaught;
        this.competitionsWon = competitionsWon;
        this.competitionsJoined = competitionsJoined;
        this.firstFish = firstFish;
        this.recentFish = recentFish;
        this.largestFish = largestFish;
        this.totalFishLength = totalFishLength;
        this.largestLength = largestLength;
        this.uuid = UUID.fromString(uuid);
        this.fishSold = fishSold;
        this.moneyEarned = moneyEarned;
    }

    public String getFirstFish() {
        return firstFish;
    }

    public void setFirstFish(String firstFish) {
        this.firstFish = firstFish;
    }

    public String getRecentFish() {
        return recentFish;
    }

    public void setRecentFish(String recentFish) {
        this.recentFish = recentFish;
    }

    public String getLargestFish() {
        return largestFish;
    }

    public void setLargestFish(String largestFish) {
        this.largestFish = largestFish;
    }

    public int getNumFishCaught() {
        return numFishCaught;
    }

    public void setNumFishCaught(int numFishCaught) {
        this.numFishCaught = numFishCaught;
    }

    public int getCompetitionsWon() {
        return competitionsWon;
    }

    public void setCompetitionsWon(int competitionsWon) {
        this.competitionsWon = competitionsWon;
    }

    public int getCompetitionsJoined() {
        return competitionsJoined;
    }

    public void setCompetitionsJoined(int competitionsJoined) {
        this.competitionsJoined = competitionsJoined;
    }

    public float getTotalFishLength() {
        return totalFishLength;
    }

    public void setTotalFishLength(float totalFishLength) {
        this.totalFishLength = totalFishLength;
    }

    public float getLargestLength() {
        return largestLength;
    }

    public void setLargestLength(float largestLength) {
        this.largestLength = largestLength;
    }

    /**
     * Increases the number of fish caught by a set amount.
     *
     * @param magnitude How many fish to increase the number caught by.
     */
    public void incrementFishCaught(final int magnitude) {
        this.numFishCaught += magnitude;
    }

    /**
     * Increases the total length of fish the player has caught.
     *
     * @param magnitude The size to increase the total size by.
     */
    public void incrementTotalLength(final float magnitude) {
        this.totalFishLength += magnitude;
    }

    /**
     * Adds a specified number to the number of competitions the user has joined.
     *
     * @param magnitude The size to increase the total number joined by.
     */
    public void incrementCompetitionsJoined(final int magnitude) {
        this.competitionsJoined += magnitude;
    }

    /**
     * Adds a specified number to the number of competitions the user has won.
     *
     * @param magnitude The size to increase the total number won by.
     */
    public void incrementCompetitionsWon(final int magnitude) {
        this.competitionsWon += magnitude;
    }

    public int getId() {
        return id;
    }

    public UUID getUUID() {
        return uuid;
    }
    
    public void incrementFishSold(final int fishSold) {
        this.fishSold += fishSold;
    }
    
    public void incrementMoneyEarned(final double moneyEarned) {
        this.moneyEarned += moneyEarned;
    }
    
    public int getFishSold() {
        return fishSold;
    }
    
    public double getMoneyEarned() {
        return moneyEarned;
    }
}
