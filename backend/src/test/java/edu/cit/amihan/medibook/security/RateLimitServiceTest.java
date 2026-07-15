package edu.cit.amihan.medibook.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RateLimitService — Brute Force Protection Tests")
class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    @DisplayName("New key is not blocked")
    void newKey_notBlocked() {
        assertFalse(rateLimitService.isBlocked("login:192.168.1.1"));
    }

    @Test
    @DisplayName("Blocks after 5 failed attempts")
    void blocksAfterMaxAttempts() {
        String key = "login:10.0.0.1";
        for (int i = 0; i < 5; i++) {
            rateLimitService.recordAttempt(key);
        }
        assertTrue(rateLimitService.isBlocked(key));
    }

    @Test
    @DisplayName("Not blocked with fewer than 5 attempts")
    void notBlockedBelowMax() {
        String key = "login:10.0.0.2";
        for (int i = 0; i < 4; i++) {
            rateLimitService.recordAttempt(key);
        }
        assertFalse(rateLimitService.isBlocked(key));
    }

    @Test
    @DisplayName("Reset clears the counter")
    void resetClearsCounter() {
        String key = "login:10.0.0.3";
        for (int i = 0; i < 5; i++) {
            rateLimitService.recordAttempt(key);
        }
        assertTrue(rateLimitService.isBlocked(key));

        rateLimitService.reset(key);
        assertFalse(rateLimitService.isBlocked(key));
    }

    @Test
    @DisplayName("Different keys are independent")
    void independentKeys() {
        String key1 = "login:10.0.0.4";
        String key2 = "login:10.0.0.5";

        for (int i = 0; i < 5; i++) {
            rateLimitService.recordAttempt(key1);
        }

        assertTrue(rateLimitService.isBlocked(key1));
        assertFalse(rateLimitService.isBlocked(key2));
    }

    @Test
    @DisplayName("Record and check are thread-safe (concurrent access)")
    void concurrentAccess() throws InterruptedException {
        String key = "login:10.0.0.6";
        int threads = 10;
        int attemptsPerThread = 100;

        Thread[] workers = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                for (int i = 0; i < attemptsPerThread; i++) {
                    rateLimitService.recordAttempt(key);
                    rateLimitService.isBlocked(key);
                }
            });
        }

        for (Thread w : workers) w.start();
        for (Thread w : workers) w.join();

        // After 1000 concurrent attempts, should definitely be blocked
        assertTrue(rateLimitService.isBlocked(key));
    }
}
