package com.oheers.fish.exceptions;

import com.oheers.fish.baits.ApplicationResult;
import com.oheers.fish.baits.Bait;

public class MaxBaitReachedException extends Exception {

    ApplicationResult recoveryResult;

    public MaxBaitReachedException(Bait bait, ApplicationResult recoveryResult) {
        super(bait.getName() + " has reached its maximum number of uses on the fishing rod.");
        this.recoveryResult = recoveryResult;
    }

    /**
     * @return The interrupted ApplicationResult object that would have been returned if it weren't for the error.
     */
    public ApplicationResult getRecoveryResult() {
        return recoveryResult;
    }
}
