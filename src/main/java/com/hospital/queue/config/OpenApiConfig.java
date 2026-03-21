package com.hospital.queue.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Hospital Queue Management System API",
        version = "1.0.0",
        description = "REST API for patient appointment booking and real-time queue management.\n\n" +
                      "**Roles:** ADMIN | DOCTOR | PATIENT\n\n" +
                      "**Default Admin:** admin@hospital.com / Admin@123\n\n" +
                      "**WebSocket:** Connect to /ws for live queue updates via STOMP",
        contact = @Contact(name = "Hospital Queue System", email = "support@hospital.com")
    ),
    servers = @Server(url = "http://localhost:8080", description = "Local Development")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Paste your JWT token here (obtained from /api/auth/login)"
)
public class OpenApiConfig {
}
