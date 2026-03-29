package com.codearena.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubmissionResponse {
    private Long id;
    private Long problemId;
    private String problemTitle;
    private Long userId;
    private String username;
    private String language;
    private String status;
    private int score;
    private int passedTests;
    private int totalTests;
    private long executionTimeMs;
    private String errorMessage;
    private String code;
    private LocalDateTime submittedAt;
}
