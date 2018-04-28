package com.github.games647.changeskin.core;

import java.util.UUID;

public class RateLimitException extends Exception {

    public static final int RATE_LIMIT_ID = 429;

    public RateLimitException(String playerName) {
        super("Rate-Limit hit on request name->uuid of" + playerName);
    }

    public RateLimitException(UUID skinId) {
        super("Rate-Limit on skin request of " + skinId);
    }
}
