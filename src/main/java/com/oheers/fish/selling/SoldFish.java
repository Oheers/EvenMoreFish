package com.oheers.fish.selling;

public class SoldFish {
    private final String name;
    private final String rarity;
    private int amount;
    
    private double totalValue;
    private double length;
    
    public SoldFish(String name, String rarity, int amount, double totalValue, double length) {
        this.name = name;
        this.rarity = rarity;
        this.amount = amount;
        this.totalValue = totalValue;
        this.length = length;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }
    
    public String getName() {
        return name;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public double getTotalValue() {
        return totalValue;
    }
    
    public String getRarity() {
        return rarity;
    }
    
    public double getLength() {
        return length;
    }
}
