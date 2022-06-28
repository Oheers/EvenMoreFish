package com.oheers.fish.database;

public class UserReport {

	private final int id;
	private String firstFish, recentFish, largestFish;
	private int numFishCaught, competitionsWon, competitionsJoined;

	private float largestLength, totalFishLength;

	public UserReport(final int id, final int numFishCaught, final int competitionsWon, final int competitionsJoined,
					  final String firstFish, final String recentFish, final String largestFish, final float totalFishLength,
					  final float largestLength) {
		this.id = id;
		this.numFishCaught = numFishCaught;
		this.competitionsWon = competitionsWon;
		this.competitionsJoined = competitionsJoined;
		this.firstFish = firstFish;
		this.recentFish = recentFish;
		this.largestFish = largestFish;
		this.totalFishLength = totalFishLength;
		this.largestLength = largestLength;
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
	 * @param magnitude How many fish to increase the number caught by.
	 */
	public void incrementFishCaught(final int magnitude) {
		this.numFishCaught += magnitude;
	}

	/**
	 * Increases the total length of fish the player has caught.
	 * @param magnitude The size to increase the total size by.
	 */
	public void incrementTotalLength(final float magnitude) {
		this.totalFishLength += magnitude;
	}

	public int getId() {
		return id;
	}
}
