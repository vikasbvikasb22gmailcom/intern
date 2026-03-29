package com.codearena.config;

import com.codearena.model.*;
import com.codearena.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedProblems();
    }

    private void seedAdmin() {
        if (userRepository.existsByUsername("admin")) return;

        User admin = User.builder()
                .username("admin")
                .email("admin@codearena.dev")
                .password(passwordEncoder.encode("admin123"))
                .role(User.Role.ADMIN)
                .build();
        userRepository.save(admin);
        leaderboardRepository.save(LeaderboardEntry.builder().user(admin).build());
        log.info("✅ Admin seeded — username: admin, password: admin123");
    }

    private void seedProblems() {
        if (problemRepository.count() > 0) return;

        User admin = userRepository.findByUsername("admin").orElseThrow();

        Problem[] problems = {
            Problem.builder()
                .title("Two Sum")
                .difficulty(Problem.Difficulty.EASY)
                .tags("array,hash-map")
                .description("""
                    Given an array of integers `nums` and an integer `target`, return **indices** of the two numbers such that they add up to target.

                    **Example:**
                    ```
                    Input:  nums = [2,7,11,15], target = 9
                    Output: [0,1]
                    ```

                    **Constraints:**
                    - 2 ≤ nums.length ≤ 10⁴
                    - Exactly one solution exists.
                    """)
                .starterCode("""
                    public int[] twoSum(int[] nums, int target) {
                        // Write your solution here
                        return new int[]{};
                    }
                    """)
                .testCases("""
                    [
                      {"input": [[2,7,11,15], 9],  "expected": [0,1]},
                      {"input": [[3,2,4], 6],       "expected": [1,2]},
                      {"input": [[3,3], 6],          "expected": [0,1]}
                    ]
                    """)
                .createdBy(admin).build(),

            Problem.builder()
                .title("Fibonacci Number")
                .difficulty(Problem.Difficulty.EASY)
                .tags("recursion,dynamic-programming")
                .description("""
                    Given `n`, return `F(n)` where F(0)=0, F(1)=1, F(n)=F(n-1)+F(n-2).

                    **Example:**
                    ```
                    Input:  n = 10
                    Output: 55
                    ```
                    """)
                .starterCode("""
                    public int fib(int n) {
                        // Write your solution here
                        return 0;
                    }
                    """)
                .testCases("""
                    [
                      {"input": [0],  "expected": 0},
                      {"input": [1],  "expected": 1},
                      {"input": [5],  "expected": 5},
                      {"input": [10], "expected": 55},
                      {"input": [15], "expected": 610}
                    ]
                    """)
                .createdBy(admin).build(),

            Problem.builder()
                .title("Valid Parentheses")
                .difficulty(Problem.Difficulty.MEDIUM)
                .tags("stack,string")
                .description("""
                    Given a string containing only `(`, `)`, `{`, `}`, `[`, `]`, determine if it is valid.

                    Open brackets must be closed by the same type and in the correct order.

                    **Example:**
                    ```
                    Input:  s = "()[]{}"
                    Output: true
                    ```
                    """)
                .starterCode("""
                    public boolean isValid(String s) {
                        // Write your solution here
                        return false;
                    }
                    """)
                .testCases("""
                    [
                      {"input": ["()"],     "expected": true},
                      {"input": ["()[]{}"], "expected": true},
                      {"input": ["(]"],     "expected": false},
                      {"input": ["{[]}"],   "expected": true},
                      {"input": ["([)]"],   "expected": false}
                    ]
                    """)
                .createdBy(admin).build(),

            Problem.builder()
                .title("Maximum Subarray")
                .difficulty(Problem.Difficulty.MEDIUM)
                .tags("array,dynamic-programming,kadane")
                .description("""
                    Given an integer array `nums`, find the subarray with the **largest sum** and return its sum.

                    **Example:**
                    ```
                    Input:  nums = [-2,1,-3,4,-1,2,1,-5,4]
                    Output: 6   (subarray [4,-1,2,1])
                    ```
                    """)
                .starterCode("""
                    public int maxSubArray(int[] nums) {
                        // Write your solution here
                        return 0;
                    }
                    """)
                .testCases("""
                    [
                      {"input": [[-2,1,-3,4,-1,2,1,-5,4]], "expected": 6},
                      {"input": [[1]],                      "expected": 1},
                      {"input": [[5,4,-1,7,8]],             "expected": 23},
                      {"input": [[-1,-2,-3]],               "expected": -1}
                    ]
                    """)
                .createdBy(admin).build(),

            Problem.builder()
                .title("Climbing Stairs")
                .difficulty(Problem.Difficulty.HARD)
                .tags("dynamic-programming,math")
                .description("""
                    You are climbing a staircase with `n` steps. Each time you can climb 1 or 2 steps.
                    In how many **distinct ways** can you climb to the top?

                    **Example:**
                    ```
                    Input:  n = 5
                    Output: 8
                    ```
                    """)
                .starterCode("""
                    public int climbStairs(int n) {
                        // Write your solution here
                        return 0;
                    }
                    """)
                .testCases("""
                    [
                      {"input": [1],  "expected": 1},
                      {"input": [2],  "expected": 2},
                      {"input": [3],  "expected": 3},
                      {"input": [5],  "expected": 8},
                      {"input": [10], "expected": 89}
                    ]
                    """)
                .createdBy(admin).build()
        };

        for (Problem p : problems) {
            problemRepository.save(p);
        }

        log.info("✅ {} sample problems seeded", problems.length);
    }
}
