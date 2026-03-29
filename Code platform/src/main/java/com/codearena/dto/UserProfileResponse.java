package com.codearena.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private int totalScore;
    private int problemsSolved;
    private int totalSubmissions;
    private int easySolved;
    private int mediumSolved;
    private int hardSolved;
    private LocalDateTime lastSubmission;
}
