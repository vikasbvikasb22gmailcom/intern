package com.codearena.service;

import com.codearena.dto.ProblemRequest;
import com.codearena.dto.ProblemResponse;
import com.codearena.model.Problem;
import com.codearena.model.Submission;
import com.codearena.model.User;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.SubmissionRepository;
import com.codearena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public List<ProblemResponse> getAllProblems(String difficulty, String tag, String keyword) {
        List<Problem> problems;

        if (keyword != null && !keyword.isBlank()) {
            problems = problemRepository.searchByKeyword(keyword);
        } else if (tag != null && !tag.isBlank()) {
            problems = problemRepository.findByTag(tag);
        } else if (difficulty != null && !difficulty.isBlank()) {
            problems = problemRepository.findByDifficultyAndActiveTrue(Problem.Difficulty.valueOf(difficulty.toUpperCase()));
        } else {
            problems = problemRepository.findByActiveTrue();
        }

        boolean isAdmin = isCurrentUserAdmin();
        return problems.stream().map(p -> toResponse(p, isAdmin)).collect(Collectors.toList());
    }

    public ProblemResponse getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));
        if (!problem.isActive() && !isCurrentUserAdmin()) {
            throw new IllegalArgumentException("Problem not found: " + id);
        }
        return toResponse(problem, isCurrentUserAdmin());
    }

    @Transactional
    public ProblemResponse createProblem(ProblemRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Admin user not found"));

        Problem problem = Problem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .tags(request.getTags())
                .testCases(request.getTestCases())
                .starterCode(request.getStarterCode())
                .solutionCode(request.getSolutionCode())
                .createdBy(admin)
                .build();

        return toResponse(problemRepository.save(problem), true);
    }

    @Transactional
    public ProblemResponse updateProblem(Long id, ProblemRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setDifficulty(request.getDifficulty());
        problem.setTags(request.getTags());
        problem.setTestCases(request.getTestCases());
        problem.setStarterCode(request.getStarterCode());
        problem.setSolutionCode(request.getSolutionCode());

        return toResponse(problemRepository.save(problem), true);
    }

    @Transactional
    public void deleteProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));
        problem.setActive(false);
        problemRepository.save(problem);
    }

    // ─── MAPPER ──────────────────────────────────────────────────────────────

    private ProblemResponse toResponse(Problem p, boolean includeTestCases) {
        long total = p.getSubmissions() != null ? p.getSubmissions().size() : 0;
        long accepted = p.getSubmissions() != null
                ? p.getSubmissions().stream().filter(s -> s.getStatus() == Submission.Status.ACCEPTED).count()
                : 0;
        double rate = total > 0 ? (accepted * 100.0 / total) : 0.0;

        return ProblemResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .difficulty(p.getDifficulty().name())
                .tags(p.getTags())
                .starterCode(p.getStarterCode())
                .testCases(includeTestCases ? p.getTestCases() : null)
                .createdBy(p.getCreatedBy() != null ? p.getCreatedBy().getUsername() : "admin")
                .createdAt(p.getCreatedAt())
                .active(p.isActive())
                .totalSubmissions((int) total)
                .acceptedSubmissions((int) accepted)
                .acceptanceRate(Math.round(rate * 10.0) / 10.0)
                .build();
    }

    private boolean isCurrentUserAdmin() {
        try {
            return SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        } catch (Exception e) {
            return false;
        }
    }
}
