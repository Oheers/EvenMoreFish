package com.oheers.fish.database;

/**
 * @author sarhatabaot
 */
public class Transaction {
    private final String id; //special class for this
    private final int userId;
    private final String timestamp;
    
    public Transaction(String id, int userId, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.timestamp = timestamp;
    }
}

