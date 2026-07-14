package edu.cit.amihan.medibook.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public TokenBlacklistService() {
        cleaner.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.HOURS);
    }

    public void blacklist(String jti) {
        if (jti != null) {
            blacklistedTokens.add(jti);
            log.debug("Token blacklisted: {}", jti);
        }
    }

    public boolean isBlacklisted(String jti) {
        return jti != null && blacklistedTokens.contains(jti);
    }

    private void cleanup() {
        log.debug("Blacklist cleanup — {} tokens tracked", blacklistedTokens.size());
    }
}
