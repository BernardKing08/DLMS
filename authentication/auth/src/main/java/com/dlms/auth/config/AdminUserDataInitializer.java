package com.dlms.auth.config;

import com.dlms.auth.constants.AuthConstants;
import com.dlms.auth.model.Role;
import com.dlms.auth.model.User;
import com.dlms.auth.repository.RoleRepository;
import com.dlms.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds one default admin account so /admin/** pages have someone to log in
 * as immediately, without a manual DB edit. Runs after RoleDataInitializer
 * (ROLE_ADMIN must already exist) - @Order enforces that.
 *
 * Credentials are intentionally simple/well-known since this is a local
 * dev/demo system with no real authentication hardening (no JWT, no lockout,
 * plain BCrypt over HTTP within a trusted Docker network). Change the
 * password before this is ever exposed anywhere non-local.
 */
@Configuration
public class AdminUserDataInitializer {

    public static final String ADMIN_EMAIL = "admin@dlms.local";
    public static final String ADMIN_DEFAULT_PASSWORD = "admin123";

    @Bean
    @Order(2)
    CommandLineRunner initAdminUser(UserRepository userRepository,
                                     RoleRepository roleRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.existsByEmail(ADMIN_EMAIL)) {
                return;
            }

            Role adminRole = roleRepository.findByRoleName(AuthConstants.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalStateException(
                            "ROLE_ADMIN must be seeded before the admin user - check RoleDataInitializer ran first"));

            User admin = User.builder()
                    .name("Administrator")
                    .email(ADMIN_EMAIL)
                    .pwd(passwordEncoder.encode(ADMIN_DEFAULT_PASSWORD))
                    .role(adminRole)
                    .build();

            userRepository.save(admin);
        };
    }
}
