package com.codearena.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "total_score")
    @Builder.Default
    private int totalScore = 0;

    @Column(name = "problems_solved")
    @Builder.Default
    private int problemsSolved = 0;

    @Column(name = "total_submissions")
    @Builder.Default
    private int totalSubmissions = 0;

    @Column(name = "easy_solved")
    @Builder.Default
    private int easySolved = 0;

    @Column(name = "medium_solved")
    @Builder.Default
    private int mediumSolved = 0;

    @Column(name = "hard_solved")
    @Builder.Default
    private int hardSolved = 0;

    @Column(name = "last_submission")
    private LocalDateTime lastSubmission;
}
