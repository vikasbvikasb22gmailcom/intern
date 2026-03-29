package com.codearena.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "problems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "test_cases", nullable = false, columnDefinition = "TEXT")
    private String testCases; // JSON string: [{"input": [...], "expected": ...}]

    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;

    @Column(name = "solution_code", columnDefinition = "TEXT")
    private String solutionCode; // Admin reference

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Submission> submissions;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}
