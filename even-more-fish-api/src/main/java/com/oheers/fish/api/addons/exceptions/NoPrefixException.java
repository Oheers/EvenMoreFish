package com.oheers.fish.api.addons.exceptions;


/**
 * This exception is thrown when a prefix is not found.
 */
public class NoPrefixException extends Exception {
    /**
     * The prefix causing the exception.
     */
    private final String prefix;

    /**
     * Constructs a new `NoPrefixException` with the specified prefix.
     *
     * @param prefix the prefix causing the exception
     */
    public NoPrefixException(String prefix) {
        super(String.format("No such prefix: %s, did you install the addon?", prefix));
        this.prefix = prefix;
    }

    /**
     * Returns the prefix causing the exception.
     *
     * @return the prefix causing the exception
     */
    public String getPrefix() {
        return prefix;
    }
}
