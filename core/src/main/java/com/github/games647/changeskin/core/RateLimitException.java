package com.github.games647.changeskin.core;

public class RateLimitException extends Exception {

    public static final int RATE_LIMIT_ID = 429;

    public RateLimitException(String message) {
        super(message);
    }
}
