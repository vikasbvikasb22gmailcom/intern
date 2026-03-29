package com.codearena.controller;

import com.codearena.dto.*;
import com.codearena.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * POST /api/submissions
     * Submit a solution for a problem. Triggers automatic evaluation.
     */
    @PostMapping("/submissions")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submit(@Valid @RequestBody SubmissionRequest request) {
        SubmissionResponse result = submissionService.submit(request);
        String msg = result.getStatus().equals("ACCEPTED")
                ? "✅ Accepted! All test cases passed."
                : "❌ " + result.getStatus() + " — " + result.getPassedTests() + "/" + result.getTotalTests() + " tests passed.";
        return ResponseEntity.ok(ApiResponse.ok(msg, result));
    }

    /**
     * GET /api/submissions/me
     * Get all submissions by the logged-in user.
     */
    @GetMapping("/submissions/me")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getMySubmissions() {
        return ResponseEntity.ok(ApiResponse.ok(submissionService.getMySubmissions()));
    }

    /**
     * GET /api/submissions/me/problem/{problemId}
     * Get current user's submissions for a specific problem.
     */
    @GetMapping("/submissions/me/problem/{problemId}")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getMySubmissionsForProblem(@PathVariable Long problemId) {
        return ResponseEntity.ok(ApiResponse.ok(submissionService.getMySubmissionsForProblem(problemId)));
    }

    /**
     * GET /api/submissions/{id}
     * Get a specific submission (own submissions or admin).
     */
    @GetMapping("/submissions/{id}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(submissionService.getSubmissionById(id)));
    }

    /**
     * GET /api/admin/submissions
     * Admin: get all submissions across all users.
     */
    @GetMapping("/admin/submissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getAllSubmissions() {
        return ResponseEntity.ok(ApiResponse.ok(submissionService.getAllSubmissions()));
    }

    /**
     * GET /api/admin/submissions/problem/{problemId}
     * Admin: get all submissions for a specific problem.
     */
    @GetMapping("/admin/submissions/problem/{problemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissionsForProblem(@PathVariable Long problemId) {
        return ResponseEntity.ok(ApiResponse.ok(submissionService.getSubmissionsForProblem(problemId)));
    }
}
