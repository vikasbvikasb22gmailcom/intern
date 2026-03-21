package com.hospital.queue;

import com.hospital.queue.repository.UserRepository;
import com.hospital.queue.service.AuthService;
import com.hospital.queue.dto.AuthDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class HospitalQueueApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Test
    void contextLoads() {
        // Verifies the Spring application context starts without errors
    }

    @Test
    void adminUserIsCreatedOnStartup() {
        boolean adminExists = userRepository.existsByEmail("admin@hospital.com");
        assertThat(adminExists).isTrue();
    }

    @Test
    void patientRegistrationWorks() {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setFirstName("Test");
        request.setLastName("Patient");
        request.setEmail("testpatient@example.com");
        request.setPassword("test123");
        request.setPhone("9876543210");

        AuthDto.AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("testpatient@example.com");
        assertThat(response.getAccessToken()).isNotBlank();
    }
}
