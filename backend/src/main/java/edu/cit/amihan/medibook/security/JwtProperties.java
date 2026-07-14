package edu.cit.amihan.medibook.security;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {

    private final Environment environment;

    public JwtProperties(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateJwtSecret() {
        String secret = environment.getProperty("jwt.secret");
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET environment variable is not set. Generate one with: " +
                    "openssl rand -base64 64"
            );
        }
        if (secret.length() < 43) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 43 characters (256 bits base64-encoded). " +
                    "Current length: " + secret.length() + ". " +
                    "Generate a proper secret with: openssl rand -base64 64"
            );
        }
    }
}
