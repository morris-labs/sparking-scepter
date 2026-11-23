package com.southcentralpositronics.reign_gdx.engine.level;

/**
 * Thrown when an arena file can't be played: it can't be read, or it lacks
 * something an arena needs, such as a player start. The message names the
 * file and what to fix.
 */
public class InvalidArenaException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param message What's wrong and how to fix it.
     */
    public InvalidArenaException(String message) {
        super(message);
    }

    /**
     * Creates the exception for an error that another one caused.
     *
     * @param message What's wrong and how to fix it.
     * @param cause   The error that the loader ran into.
     */
    public InvalidArenaException(String message, Throwable cause) {
        super(message, cause);
    }
}
