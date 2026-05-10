package org.example.authservice.config;

import org.example.authservice.entities.Role;
import org.example.authservice.entities.User;
import org.example.authservice.repositories.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private final String adminUsername;
    private final String adminPassword;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      DefaultAdminProperties defaultAdminProperties) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = defaultAdminProperties.getUsername();
        this.adminPassword = defaultAdminProperties.getPassword();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByRoleAndIsProtected(Role.ADMIN, true)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .isProtected(true)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }
}
