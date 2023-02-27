package com.oheers.fish.selling;

public class SoldFish {
    private final String name;
    private final String rarity;
    private int amount;
    
    private double totalValue;
    
    public SoldFish(String name, String rarity, int amount, double totalValue) {
        this.name = name;
        this.rarity = rarity;
        this.amount = amount;
        this.totalValue = totalValue;
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
}
