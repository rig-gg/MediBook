package edu.cit.amihan.medibook.security;

import edu.cit.amihan.medibook.security.entity.BlacklistedToken;
import edu.cit.amihan.medibook.security.repository.BlacklistedTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistRepository;

    public void blacklist(String jti, LocalDateTime expiresAt) {
        if (jti != null && !blacklistRepository.existsByJti(jti)) {
            blacklistRepository.save(new BlacklistedToken(jti, expiresAt));
            log.debug("Token blacklisted: {}", jti);
        }
    }

    public boolean isBlacklisted(String jti) {
        return jti != null && blacklistRepository.existsByJti(jti);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanup() {
        blacklistRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.debug("Blacklist cleanup completed");
    }
}
