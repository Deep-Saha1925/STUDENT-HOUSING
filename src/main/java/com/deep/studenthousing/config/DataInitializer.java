package com.deep.studenthousing.config;

import com.deep.studenthousing.entity.Role;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Falls back to the old hardcoded default if ADMIN_USERNAME isn't set,
        // so existing deployments that only ever set ADMIN_PASSWORD keep working
        // unchanged — this only takes effect once you add the new env var.
        String rawUsername = System.getenv("ADMIN_USERNAME");
        String adminEmail = (rawUsername == null || rawUsername.isBlank())
                ? "admin@studenthousing.com"
                : rawUsername.trim();

        String adminPassword = System.getenv("ADMIN_PASSWORD");

        log.info("[DataInitializer] ADMIN_USERNAME env var present: {}", rawUsername != null);
        log.info("[DataInitializer] ADMIN_PASSWORD env var present: {}", adminPassword != null && !adminPassword.isBlank());
        log.info("[DataInitializer] Resolved admin email to seed/check: '{}'", adminEmail);

        if (adminPassword == null || adminPassword.isBlank()) {
            log.error("[DataInitializer] ADMIN_PASSWORD is missing or blank — skipping admin creation entirely. " +
                    "Set it as an environment variable and redeploy.");
            return;
        }

        User existing = userRepository.findByEmail(adminEmail);
        if (existing == null) {
            User admin = new User();
            admin.setFullName("Deep Saha");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setPhone("0000000000");
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
            log.info("[DataInitializer] Created new admin account for '{}'.", adminEmail);
        } else {
            log.info("[DataInitializer] Admin account for '{}' already exists (id={}) — not modified. " +
                    "Role on record: {}", adminEmail, existing.getId(), existing.getRole());
        }
    }
}