package com.hospital.queue.dto;

import com.hospital.queue.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @Email(message = "Valid email is required")
        @NotBlank(message = "Email is required")
        private String email;

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Valid 10-digit phone number required")
        private String phone;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private String address;
        private Integer age;
        private String bloodGroup;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
        private Role role;
    }

    @Data
    public static class RefreshTokenRequest {
        @NotBlank
        private String refreshToken;
    }
}
