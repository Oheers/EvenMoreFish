package com.oheers.fish.exceptions;

import com.oheers.fish.baits.ApplicationResult;
import com.oheers.fish.baits.Bait;
import org.jetbrains.annotations.NotNull;

public class MaxBaitReachedException extends Exception {

    private final ApplicationResult recoveryResult;

    public MaxBaitReachedException(@NotNull Bait bait, @NotNull ApplicationResult recoveryResult) {
        super(bait.getName() + " has reached its maximum number of uses on the fishing rod.");
        this.recoveryResult = recoveryResult;
    }

    /**
     * @return The interrupted ApplicationResult object that would have been returned if it weren't for the error.
     */
    public @NotNull ApplicationResult getRecoveryResult() {
        return recoveryResult;
    }

}
