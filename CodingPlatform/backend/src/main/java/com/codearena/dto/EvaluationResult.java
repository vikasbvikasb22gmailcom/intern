package com.codearena.dto;

import com.codearena.model.Submission;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvaluationResult {
    private boolean accepted;
    private int passedTests;
    private int totalTests;
    private int score;
    private long executionTimeMs;
    private Submission.Status status;
    private String errorMessage;
    private List<TestCaseResult> testResults;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TestCaseResult {
        private int testNumber;
        private boolean passed;
        private String input;
        private String expected;
        private String actual;
        private String error;
    }
}
