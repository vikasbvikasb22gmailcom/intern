package com.codearena.dto;

import com.codearena.model.Problem;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProblemRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private Problem.Difficulty difficulty = Problem.Difficulty.MEDIUM;

    private String tags;

    @NotBlank(message = "Test cases are required (JSON array)")
    private String testCases;

    private String starterCode;

    private String solutionCode;
}
