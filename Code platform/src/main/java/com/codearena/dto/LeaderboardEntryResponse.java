package com.codearena.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaderboardEntryResponse {
    private int rank;
    private Long userId;
    private String username;
    private int totalScore;
    private int problemsSolved;
    private int totalSubmissions;
    private int easySolved;
    private int mediumSolved;
    private int hardSolved;
    private LocalDateTime lastSubmission;
}
