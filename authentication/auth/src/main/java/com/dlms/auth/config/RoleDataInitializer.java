package com.dlms.auth.config;

import com.dlms.auth.constants.AuthConstants;
import com.dlms.auth.model.Role;
import com.dlms.auth.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class RoleDataInitializer {

    @Bean
    @Order(1)
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {

            if (roleRepository.findByRoleName(AuthConstants.ROLE_USER).isEmpty()) {
                Role userRole = new Role();
                userRole.setRoleName(AuthConstants.ROLE_USER);
                roleRepository.save(userRole);
            }

            if (roleRepository.findByRoleName(AuthConstants.ROLE_ADMIN).isEmpty()) {
                Role adminRole = new Role();
                adminRole.setRoleName(AuthConstants.ROLE_ADMIN);
                roleRepository.save(adminRole);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // REST API → disable CSRF
            .csrf(csrf -> csrf.disable())

            // Explicit authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().permitAll()
            )

            // No login form, no HTTP basic
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form.disable());

        return http.build();
    }
}
