package edu.cit.amihan.medibook.security.repository;

import edu.cit.amihan.medibook.security.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByJti(String jti);

    void deleteByExpiresAtBefore(java.time.LocalDateTime cutoff);
}
