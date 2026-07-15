package edu.cit.amihan.medibook.security.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_tokens", indexes = {
        @Index(name = "idx_jti", columnList = "jti"),
        @Index(name = "idx_expires_at", columnList = "expiresAt")
})
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jti;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime blacklistedAt;

    public BlacklistedToken() {}

    public BlacklistedToken(String jti, LocalDateTime expiresAt) {
        this.jti = jti;
        this.expiresAt = expiresAt;
        this.blacklistedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getJti() { return jti; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getBlacklistedAt() { return blacklistedAt; }
}
