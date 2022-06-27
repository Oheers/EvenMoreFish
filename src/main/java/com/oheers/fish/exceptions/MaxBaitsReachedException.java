package com.oheers.fish.exceptions;

import com.oheers.fish.baits.ApplicationResult;

public class MaxBaitsReachedException extends Exception {
	ApplicationResult recoveryResult;

	public MaxBaitsReachedException(String errorMessage, ApplicationResult recoveryResult) {
		super(errorMessage);
		this.recoveryResult = recoveryResult;
	}

	public ApplicationResult getRecoveryResult() {
		return recoveryResult;
	}
}
