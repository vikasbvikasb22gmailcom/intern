package com.codearena.service;

import com.codearena.dto.LeaderboardEntryResponse;
import com.codearena.dto.UserProfileResponse;
import com.codearena.model.LeaderboardEntry;
import com.codearena.model.User;
import com.codearena.repository.LeaderboardRepository;
import com.codearena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;

    public List<LeaderboardEntryResponse> getLeaderboard() {
        List<LeaderboardEntry> entries = leaderboardRepository.findAllOrderByRank();
        List<LeaderboardEntryResponse> result = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            result.add(toResponse(entries.get(i), i + 1));
        }
        return result;
    }

    public List<LeaderboardEntryResponse> getTopN(int n) {
        List<LeaderboardEntry> entries = leaderboardRepository.findTopN(Math.min(n, 100));
        List<LeaderboardEntryResponse> result = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            result.add(toResponse(entries.get(i), i + 1));
        }
        return result;
    }

    public UserProfileResponse getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        LeaderboardEntry entry = leaderboardRepository.findByUser(user)
                .orElseGet(() -> LeaderboardEntry.builder().user(user).build());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .totalScore(entry.getTotalScore())
                .problemsSolved(entry.getProblemsSolved())
                .totalSubmissions(entry.getTotalSubmissions())
                .easySolved(entry.getEasySolved())
                .mediumSolved(entry.getMediumSolved())
                .hardSolved(entry.getHardSolved())
                .lastSubmission(entry.getLastSubmission())
                .build();
    }

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        LeaderboardEntry entry = leaderboardRepository.findByUser(user)
                .orElseGet(() -> LeaderboardEntry.builder().user(user).build());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .totalScore(entry.getTotalScore())
                .problemsSolved(entry.getProblemsSolved())
                .totalSubmissions(entry.getTotalSubmissions())
                .easySolved(entry.getEasySolved())
                .mediumSolved(entry.getMediumSolved())
                .hardSolved(entry.getHardSolved())
                .lastSubmission(entry.getLastSubmission())
                .build();
    }

    private LeaderboardEntryResponse toResponse(LeaderboardEntry e, int rank) {
        return LeaderboardEntryResponse.builder()
                .rank(rank)
                .userId(e.getUser().getId())
                .username(e.getUser().getUsername())
                .totalScore(e.getTotalScore())
                .problemsSolved(e.getProblemsSolved())
                .totalSubmissions(e.getTotalSubmissions())
                .easySolved(e.getEasySolved())
                .mediumSolved(e.getMediumSolved())
                .hardSolved(e.getHardSolved())
                .lastSubmission(e.getLastSubmission())
                .build();
    }
}
