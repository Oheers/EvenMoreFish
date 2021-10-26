package com.oheers.fish.database;

import com.oheers.fish.fishing.items.Fish;

public class FishReport {

	String rarity, name;
	int numCaught;
	float largestLength;

	public FishReport(String rarity, String name, float size, int numCaught) {
		this.rarity = rarity;
		this.name = name;
		this.numCaught = numCaught;
		this.largestLength = size;
	}

	public int getNumCaught() {
		return numCaught;
	}

	public void setNumCaught(int numCaught) {
		this.numCaught = numCaught;
	}

	public String getRarity() {
		return rarity;
	}

	public String getName() {
		return name;
	}

	public float getLargestLength() {
		return largestLength;
	}

	public void setLargestLength(float largestLength) {
		this.largestLength = largestLength;
	}

	public void addFish(Fish f) {
		if (f.getLength() > this.largestLength) {
			this.largestLength = f.getLength();
		}
		numCaught++;
	}

	@Override
	public String toString() {
		return "FishReport=[name:" + name + ", rarity:" + rarity + ", largestLength:" + largestLength + ", numCaught:" + numCaught;
	}
}
