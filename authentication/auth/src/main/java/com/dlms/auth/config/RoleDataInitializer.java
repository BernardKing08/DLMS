package com.dlms.auth.config;

import com.dlms.auth.constants.AuthConstants;
import com.dlms.auth.model.Role;
import com.dlms.auth.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Configuration;

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
}
