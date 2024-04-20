package com.oheers.fish.database;

import java.sql.Timestamp;

public class Transaction {
    private final String id; //special class for this
    private final int userId;
    private final Timestamp timestamp;
    
    public Transaction(String id, int userId, Timestamp timestamp) {
        this.id = id;
        this.userId = userId;
        this.timestamp = timestamp;
    }
    
    public String getId() {
        return id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public Timestamp getTimestamp() {
        return timestamp;
    }
}

