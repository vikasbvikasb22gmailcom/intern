package com.codearena.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProblemResponse {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private String tags;
    private String starterCode;
    private String testCases;       // only set for admin
    private String createdBy;
    private LocalDateTime createdAt;
    private boolean active;
    private int totalSubmissions;
    private int acceptedSubmissions;
    private double acceptanceRate;
}
