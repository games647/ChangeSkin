package com.github.games647.changeskin.core;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

public class RateLimiterTest {

    private RateLimiter limiter;

    @Test
    public void testMaxSize() {
        limiter = new RateLimiter(Duration.ofMinutes(2), 3);
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.tryAcquire(), is(true));
        }

        assertThat(limiter.tryAcquire(), is(false));
    }

    @Test
    public void testExpiring() throws InterruptedException {
        limiter = new RateLimiter(Duration.ofSeconds(1), 3);
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.tryAcquire(), is(true));
        }

        assertThat(limiter.tryAcquire(), is(false));
        TimeUnit.SECONDS.sleep(1);
        assertThat(limiter.tryAcquire(), is(true));
    }

    @Test
    public void testNoRequests() {
        limiter = new RateLimiter(Duration.ofMinutes(2), 0);
        assertThat(limiter.tryAcquire(), is(false));

        limiter = new RateLimiter(Duration.ofMinutes(2), -50);
        assertThat(limiter.tryAcquire(), is(false));
    }
}
