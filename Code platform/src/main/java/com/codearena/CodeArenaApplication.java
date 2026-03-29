package com.codearena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodeArenaApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeArenaApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  CodeArena Platform Started!");
        System.out.println("  API Base: http://localhost:8080/api");
        System.out.println("  H2 Console: http://localhost:8080/h2-console");
        System.out.println("  Default Admin: admin / admin123");
        System.out.println("========================================\n");
    }
}
