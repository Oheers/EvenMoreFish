package com.oheers.fish.database;

import com.oheers.fish.fishing.items.Fish;

public class UserReport {

	private final int id;
	private Fish firstFish, recentFish, largestFish;
	private int numFishCaught, competitionsWon, competitionsJoined;

	public UserReport(final int id, final int numFishCaught, final int competitionsWon, final int competitionsJoined,
					  final Fish firstFish, final Fish recentFish, final Fish largestFish) {
		this.id = id;
		this.numFishCaught = numFishCaught;
		this.competitionsWon = competitionsWon;
		this.competitionsJoined = competitionsJoined;
		this.firstFish = firstFish;
		this.recentFish = recentFish;
		this.largestFish = largestFish;
	}
}
