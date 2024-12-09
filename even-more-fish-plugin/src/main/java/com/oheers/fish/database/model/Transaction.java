package com.oheers.fish.database.model;

import java.sql.Timestamp;

/**
 * @param id special class for this
 */
public record Transaction(String id, int userId, Timestamp timestamp) {
}

