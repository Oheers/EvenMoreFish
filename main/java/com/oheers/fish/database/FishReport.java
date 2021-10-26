package com.oheers.fish.database;

import com.oheers.fish.fishing.items.Fish;

public class FishReport {

	String r, n;
	int c;
	float l;

	public FishReport(String rarity, String name, float size, int numCaught) {
		this.r = rarity;
		this.n = name;
		this.c = numCaught;
		this.l = size;
	}

	public int getNumCaught() {
		return c;
	}

	public void setNumCaught(int numCaught) {
		this.c = numCaught;
	}

	public String getRarity() {
		return r;
	}

	public String getName() {
		return n;
	}

	public float getLargestLength() {
		return l;
	}

	public void setLargestLength(float largestLength) {
		this.l = largestLength;
	}

	public void addFish(Fish f) {
		if (f.getLength() > this.l) {
			this.l = f.getLength();
		}
		c++;
	}

	@Override
	public String toString() {
		return "FishReport=[name:" + n + ", rarity:" + r + ", largestLength:" + l + ", numCaught:" + c;
	}
}
