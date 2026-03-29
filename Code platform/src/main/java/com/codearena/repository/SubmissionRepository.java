package com.codearena.repository;

import com.codearena.model.Submission;
import com.codearena.model.User;
import com.codearena.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUserOrderBySubmittedAtDesc(User user);

    List<Submission> findByProblemOrderBySubmittedAtDesc(Problem problem);

    List<Submission> findByUserAndProblemOrderBySubmittedAtDesc(User user, Problem problem);

    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId ORDER BY s.submittedAt DESC")
    List<Submission> findByUserId(Long userId);

    @Query("SELECT s FROM Submission s WHERE s.problem.id = :problemId ORDER BY s.submittedAt DESC")
    List<Submission> findByProblemId(Long problemId);

    Optional<Submission> findTopByUserAndProblemAndStatusOrderByScoreDesc(
        User user, Problem problem, Submission.Status status);

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED'")
    int countDistinctSolvedProblemsByUserId(Long userId);

    boolean existsByUserAndProblemAndStatus(User user, Problem problem, Submission.Status status);
}
