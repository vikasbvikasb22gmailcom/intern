package com.codearena.repository;

import com.codearena.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByActiveTrue();

    List<Problem> findByDifficultyAndActiveTrue(Problem.Difficulty difficulty);

    @Query("SELECT p FROM Problem p WHERE p.active = true AND LOWER(p.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<Problem> findByTag(String tag);

    @Query("SELECT p FROM Problem p WHERE p.active = true AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Problem> searchByKeyword(String keyword);
}
