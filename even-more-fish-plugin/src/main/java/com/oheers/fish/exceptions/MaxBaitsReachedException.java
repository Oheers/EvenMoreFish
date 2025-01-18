package com.oheers.fish.exceptions;

import com.oheers.fish.baits.ApplicationResult;
import org.jetbrains.annotations.NotNull;

public class MaxBaitsReachedException extends Exception {

    private final ApplicationResult recoveryResult;

    public MaxBaitsReachedException(@NotNull String errorMessage, @NotNull ApplicationResult recoveryResult) {
        super(errorMessage);
        this.recoveryResult = recoveryResult;
    }

    public @NotNull ApplicationResult getRecoveryResult() {
        return recoveryResult;
    }
}
