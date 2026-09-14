package com.deep.studenthousing.config;

import com.deep.studenthousing.entity.Role;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

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
        String adminEmail = System.getenv("ADMIN_USERNAME");
        if (adminEmail == null || adminEmail.isBlank()) {
            adminEmail = "admin@studenthousing.com";
        }
        String adminPassword = System.getenv("ADMIN_PASSWORD");

        if (userRepository.findByEmail(adminEmail) == null) {
            User admin = new User();
            admin.setFullName("Deep Saha");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setPhone("0000000000");
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
        }
    }

}