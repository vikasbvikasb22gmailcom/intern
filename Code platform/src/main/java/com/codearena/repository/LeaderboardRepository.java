package com.codearena.repository;

import com.codearena.model.LeaderboardEntry;
import com.codearena.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardEntry, Long> {

    Optional<LeaderboardEntry> findByUser(User user);

    Optional<LeaderboardEntry> findByUserId(Long userId);

    @Query("SELECT l FROM LeaderboardEntry l ORDER BY l.totalScore DESC, l.problemsSolved DESC")
    List<LeaderboardEntry> findAllOrderByRank();

    @Query("SELECT l FROM LeaderboardEntry l ORDER BY l.totalScore DESC, l.problemsSolved DESC LIMIT :limit")
    List<LeaderboardEntry> findTopN(int limit);
}
