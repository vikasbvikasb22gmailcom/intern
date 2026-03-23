package com.hospital.queue.config;

import com.hospital.queue.entity.User;
import com.hospital.queue.enums.Role;
import com.hospital.queue.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true);
        return mapper;
    }

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Create default admin if not exists
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = User.builder()
                        .firstName("System")
                        .lastName("Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ROLE_ADMIN)
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                log.info("Default admin created with email: {}", adminEmail);
            }
        };
    }
}
