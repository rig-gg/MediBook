package edu.cit.amihan.medibook.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RateLimitService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000;

    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        AttemptInfo info = attempts.get(key);
        if (info == null) return false;

        if (System.currentTimeMillis() - info.windowStart > WINDOW_MS) {
            attempts.remove(key);
            return false;
        }
        return info.count >= MAX_ATTEMPTS;
    }

    public void recordAttempt(String key) {
        attempts.compute(key, (k, existing) -> {
            if (existing == null || System.currentTimeMillis() - existing.windowStart > WINDOW_MS) {
                return new AttemptInfo(1, System.currentTimeMillis());
            }
            existing.count++;
            return existing;
        });
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private static class AttemptInfo {
        int count;
        long windowStart;

        AttemptInfo(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
