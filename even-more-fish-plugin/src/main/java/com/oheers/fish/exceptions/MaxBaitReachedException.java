package com.oheers.fish.exceptions;

import com.oheers.fish.baits.ApplicationResult;

public class MaxBaitReachedException extends Exception {

    ApplicationResult recoveryResult;

    public MaxBaitReachedException(String errorMessage, ApplicationResult recoveryResult) {
        super(errorMessage);
        this.recoveryResult = recoveryResult;
    }

    /**
     * @return The interrupted ApplicationResult object that would have been returned if it weren't for the error.
     */
    public ApplicationResult getRecoveryResult() {
        return recoveryResult;
    }
}
