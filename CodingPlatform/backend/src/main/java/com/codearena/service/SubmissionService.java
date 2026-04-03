package com.codearena.service;

import com.codearena.dto.EvaluationResult;
import com.codearena.dto.SubmissionRequest;
import com.codearena.dto.SubmissionResponse;
import com.codearena.evaluator.CodeEvaluator;
import com.codearena.model.*;
import com.codearena.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final CodeEvaluator codeEvaluator;

    @Transactional
    public SubmissionResponse submit(SubmissionRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + request.getProblemId()));

        if (!problem.isActive()) {
            throw new IllegalArgumentException("Problem is not active.");
        }

        // ✅ FIX: Check if already solved BEFORE saving this submission
        boolean wasAlreadySolved = submissionRepository.existsByUserAndProblemAndStatus(
                user, problem, Submission.Status.ACCEPTED);

        // Save as PENDING first
        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .code(request.getCode())
                .language(request.getLanguage())
                .status(Submission.Status.PENDING)
                .build();
        submission = submissionRepository.save(submission);

        // Evaluate
        log.info("Evaluating submission {} for user {} on problem {}", submission.getId(), username, problem.getTitle());
        long start = System.currentTimeMillis();

        EvaluationResult result = codeEvaluator.evaluate(
                request.getCode(),
                problem.getTestCases(),
                request.getLanguage()
        );
        result.setExecutionTimeMs(System.currentTimeMillis() - start);

        // Update submission with result
        submission.setStatus(result.getStatus());
        submission.setScore(result.getScore());
        submission.setPassedTests(result.getPassedTests());
        submission.setTotalTests(result.getTotalTests());
        submission.setExecutionTimeMs(result.getExecutionTimeMs());
        submission.setErrorMessage(result.getErrorMessage());
        submissionRepository.save(submission);

        // Update leaderboard using the pre-checked flag
        updateLeaderboard(user, problem, result, wasAlreadySolved);

        log.info("Submission {}: {} ({}/{} tests)", submission.getId(), result.getStatus(),
                result.getPassedTests(), result.getTotalTests());

        return toResponse(submission);
    }

    public List<SubmissionResponse> getMySubmissions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return submissionRepository.findByUserOrderBySubmittedAtDesc(user)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SubmissionResponse> getMySubmissionsForProblem(Long problemId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + problemId));
        return submissionRepository.findByUserAndProblemOrderBySubmittedAtDesc(user, problem)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SubmissionResponse> getAllSubmissions() {
        return submissionRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<SubmissionResponse> getSubmissionsForProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + problemId));
        return submissionRepository.findByProblemOrderBySubmittedAtDesc(problem)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SubmissionResponse getSubmissionById(Long id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !s.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied.");
        }
        return toResponse(s);
    }

    // ─── LEADERBOARD UPDATE ──────────────────────────────────────────────────

    private void updateLeaderboard(User user, Problem problem, EvaluationResult result, boolean wasAlreadySolved) {
        LeaderboardEntry entry = leaderboardRepository.findByUser(user)
                .orElseGet(() -> LeaderboardEntry.builder().user(user).build());

        entry.setTotalSubmissions(entry.getTotalSubmissions() + 1);
        entry.setLastSubmission(LocalDateTime.now());

        // Only award score/solved if ACCEPTED for the FIRST time
        if (result.getStatus() == Submission.Status.ACCEPTED && !wasAlreadySolved) {
            entry.setProblemsSolved(entry.getProblemsSolved() + 1);
            int points = switch (problem.getDifficulty()) {
                case EASY   -> 10;
                case MEDIUM -> 25;
                case HARD   -> 50;
            };
            entry.setTotalScore(entry.getTotalScore() + points);
            switch (problem.getDifficulty()) {
                case EASY   -> entry.setEasySolved(entry.getEasySolved() + 1);
                case MEDIUM -> entry.setMediumSolved(entry.getMediumSolved() + 1);
                case HARD   -> entry.setHardSolved(entry.getHardSolved() + 1);
            }
        }

        leaderboardRepository.save(entry);
    }

    // ─── MAPPER ──────────────────────────────────────────────────────────────

    private SubmissionResponse toResponse(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .problemId(s.getProblem().getId())
                .problemTitle(s.getProblem().getTitle())
                .userId(s.getUser().getId())
                .username(s.getUser().getUsername())
                .language(s.getLanguage().name())
                .status(s.getStatus().name())
                .score(s.getScore())
                .passedTests(s.getPassedTests())
                .totalTests(s.getTotalTests())
                .executionTimeMs(s.getExecutionTimeMs())
                .errorMessage(s.getErrorMessage())
                .code(s.getCode())
                .submittedAt(s.getSubmittedAt())
                .build();
    }
}
