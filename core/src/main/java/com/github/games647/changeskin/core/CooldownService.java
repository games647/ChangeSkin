package com.github.games647.changeskin.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownService {

    private final Cache<Object, Object> cooldowns;

    public CooldownService(Duration duration) {
        long removalTime = duration.getSeconds();
        if (removalTime <= 0) {
            removalTime = 1;
        }

        this.cooldowns = CacheBuilder.newBuilder()
                .expireAfterWrite(removalTime, TimeUnit.SECONDS)
                .build();
    }

    public void trackPlayer(UUID uniqueId) {
        cooldowns.put(uniqueId, new Object());
    }

    public boolean isTracked(UUID uniqueId) {
        return cooldowns.getIfPresent(uniqueId) != null;
    }
}
