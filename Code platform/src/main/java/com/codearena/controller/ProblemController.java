package com.codearena.controller;

import com.codearena.dto.*;
import com.codearena.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    /**
     * GET /api/problems
     * List all active problems. Optional filters: ?difficulty=EASY &tag=array &keyword=sum
     */
    @GetMapping("/problems")
    public ResponseEntity<ApiResponse<List<ProblemResponse>>> getAllProblems(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(problemService.getAllProblems(difficulty, tag, keyword)));
    }

    /**
     * GET /api/problems/{id}
     * Get a single problem by ID.
     */
    @GetMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(problemService.getProblemById(id)));
    }

    /**
     * POST /api/admin/problems
     * Admin: create a new problem.
     */
    @PostMapping("/admin/problems")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProblemResponse>> createProblem(@Valid @RequestBody ProblemRequest request) {
        ProblemResponse created = problemService.createProblem(request);
        return ResponseEntity.ok(ApiResponse.ok("Problem created successfully.", created));
    }

    /**
     * PUT /api/admin/problems/{id}
     * Admin: update an existing problem.
     */
    @PutMapping("/admin/problems/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProblemResponse>> updateProblem(
            @PathVariable Long id, @Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Problem updated.", problemService.updateProblem(id, request)));
    }

    /**
     * DELETE /api/admin/problems/{id}
     * Admin: soft-delete a problem.
     */
    @DeleteMapping("/admin/problems/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.ok(ApiResponse.ok("Problem deleted.", null));
    }
}
