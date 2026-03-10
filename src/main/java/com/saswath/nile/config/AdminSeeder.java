package com.saswath.nile.config;

import com.saswath.nile.entity.User;
import com.saswath.nile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments arguments) {
        if (userRepository.findByEmail("admin@nile.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@nile.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            System.out.println("Admin user seeded.");
        }

        if (userRepository.findByEmail("e2e.admin@nile.com").isEmpty()) {
            User testAdmin = new User();
            testAdmin.setName("E2E Admin");
            testAdmin.setEmail("e2e.admin@nile.com");
            testAdmin.setPassword(passwordEncoder.encode("E2eAdminPass1!"));
            testAdmin.setRole("ROLE_ADMIN");
            userRepository.save(testAdmin);
            System.out.println("Test admin user seeded");
        }
    }
}
