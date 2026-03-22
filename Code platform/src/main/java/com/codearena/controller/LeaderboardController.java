package com.codearena.controller;

import com.codearena.dto.*;
import com.codearena.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /**
     * GET /api/leaderboard
     * Full ranked leaderboard (public).
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> getLeaderboard() {
        return ResponseEntity.ok(ApiResponse.ok(leaderboardService.getLeaderboard()));
    }

    /**
     * GET /api/leaderboard/top?n=10
     * Top N users (public).
     */
    @GetMapping("/leaderboard/top")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> getTopN(
            @RequestParam(defaultValue = "10") int n) {
        return ResponseEntity.ok(ApiResponse.ok(leaderboardService.getTopN(n)));
    }

    /**
     * GET /api/users/me
     * Logged-in user's profile and stats.
     */
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.ok(leaderboardService.getMyProfile()));
    }

    /**
     * GET /api/users/{id}
     * Public user profile by ID.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(leaderboardService.getUserProfile(id)));
    }
}
