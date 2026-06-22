package com.jana.hospital_management.security;

import com.jana.hospital_management.entity.Role;
import com.jana.hospital_management.entity.User;
import com.jana.hospital_management.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner createDefaultAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (userRepository.existsByEmail("admin@hospital.com")) {
                return;
            }

            User admin = User.builder()
                    .email("admin@hospital.com")
                    .password(passwordEncoder.encode("Admin123"))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);

            System.out.println("Default admin created.");
        };
    }
}