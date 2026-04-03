# 🏟️ CodeArena — Mini Coding Platform (Java Spring Boot)

A fully functional backend for a competitive coding platform built with **Spring Boot 3**, **Spring Security (JWT)**, **JPA/Hibernate**, and **H2 Database**.

---

## 📁 Project Structure

```
CodeArena/
├── pom.xml
└── src/main/
    ├── java/com/codearena/
    │   ├── CodeArenaApplication.java       ← Entry point
    │   ├── config/
    │   │   ├── SecurityConfig.java         ← JWT + CORS + role-based access
    │   │   ├── GlobalExceptionHandler.java ← Centralized error handling
    │   │   └── DataInitializer.java        ← Seeds admin + sample problems
    │   ├── controller/
    │   │   ├── AuthController.java         ← Register / Login
    │   │   ├── ProblemController.java      ← Problem CRUD
    │   │   ├── SubmissionController.java   ← Submit & evaluate code
    │   │   └── LeaderboardController.java  ← Rankings & user profiles
    │   ├── service/
    │   │   ├── AuthService.java
    │   │   ├── ProblemService.java
    │   │   ├── SubmissionService.java
    │   │   └── LeaderboardService.java
    │   ├── evaluator/
    │   │   └── CodeEvaluator.java          ← Compiles & runs Java code dynamically
    │   ├── model/
    │   │   ├── User.java
    │   │   ├── Problem.java
    │   │   ├── Submission.java
    │   │   └── LeaderboardEntry.java
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   ├── ProblemRepository.java
    │   │   ├── SubmissionRepository.java
    │   │   └── LeaderboardRepository.java
    │   ├── dto/
    │   │   ├── RegisterRequest.java
    │   │   ├── LoginRequest.java
    │   │   ├── AuthResponse.java
    │   │   ├── ProblemRequest.java
    │   │   ├── ProblemResponse.java
    │   │   ├── SubmissionRequest.java
    │   │   ├── SubmissionResponse.java
    │   │   ├── EvaluationResult.java
    │   │   ├── LeaderboardEntryResponse.java
    │   │   ├── UserProfileResponse.java
    │   │   └── ApiResponse.java
    │   └── security/
    │       ├── JwtUtils.java
    │       ├── JwtAuthFilter.java
    │       └── UserDetailsServiceImpl.java
    └── resources/
        └── application.properties
```

---

## ⚙️ Prerequisites

| Tool | Version |
|------|---------|
| **JDK** | 17+ (must be JDK, not JRE — needed for `javax.tools.JavaCompiler`) |
| **Maven** | 3.8+ |
| **VS Code** | with Extension Pack for Java |

> ⚠️ **Important:** The code evaluator uses `javax.tools.JavaCompiler` which requires a full **JDK** installation, not just a JRE.

---

## 🚀 Running in VS Code

### Option 1 — VS Code (Recommended)
1. Open the `CodeArena` folder in VS Code
2. Install **Extension Pack for Java** if prompted
3. Wait for Maven to download dependencies
4. Click **Run** on `CodeArenaApplication.java`, or press `F5`

### Option 2 — Terminal
```bash
cd CodeArena
mvn spring-boot:run
```

### Option 3 — Build & Run JAR
```bash
cd CodeArena
mvn clean package -DskipTests
java -jar target/code-arena-1.0.0.jar
```

Server starts at: **http://localhost:8080**

---

## 🔑 Default Credentials

| Role  | Username | Password  |
|-------|----------|-----------|
| Admin | `admin`  | `admin123`|

---

## 📡 API Reference

### Base URL: `http://localhost:8080/api`

All responses follow this shape:
```json
{
  "success": true,
  "message": "...",
  "data": { ... }
}
```

---

### 🔐 Auth Endpoints

#### Register
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "alice",
  "email": "alice@example.com",
  "password": "secret123"
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "secret123"
}
```
**Response includes a JWT token** — use it in subsequent requests:
```
Authorization: Bearer <token>
```

---

### 📋 Problem Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/problems` | Public | List all problems |
| GET | `/api/problems?difficulty=EASY` | Public | Filter by difficulty |
| GET | `/api/problems?tag=array` | Public | Filter by tag |
| GET | `/api/problems?keyword=sum` | Public | Search by keyword |
| GET | `/api/problems/{id}` | Public | Get problem details |
| POST | `/api/admin/problems` | Admin | Create problem |
| PUT | `/api/admin/problems/{id}` | Admin | Update problem |
| DELETE | `/api/admin/problems/{id}` | Admin | Soft-delete problem |

#### Create Problem (Admin)
```
POST /api/admin/problems
Authorization: Bearer <admin-token>

{
  "title": "Two Sum",
  "description": "Find two numbers that add up to target...",
  "difficulty": "EASY",
  "tags": "array,hash-map",
  "starterCode": "public int[] twoSum(int[] nums, int target) {\n    return new int[]{};\n}",
  "testCases": "[{\"input\": [[2,7,11,15], 9], \"expected\": [0,1]}]"
}
```

**Test Cases Format (JSON Array):**
```json
[
  { "input": [[2,7,11,15], 9], "expected": [0,1] },
  { "input": [[3,2,4], 6],    "expected": [1,2]  }
]
```
- `input` → array of arguments passed to the method
- `expected` → expected return value

---

### 📤 Submission Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/submissions` | User | Submit solution (auto-evaluated) |
| GET | `/api/submissions/me` | User | My submission history |
| GET | `/api/submissions/me/problem/{id}` | User | My submissions for a problem |
| GET | `/api/submissions/{id}` | User | Get specific submission |
| GET | `/api/admin/submissions` | Admin | All submissions |
| GET | `/api/admin/submissions/problem/{id}` | Admin | Submissions for a problem |

#### Submit Solution
```
POST /api/submissions
Authorization: Bearer <token>

{
  "problemId": 1,
  "language": "JAVA",
  "code": "public int[] twoSum(int[] nums, int target) {\n    Map<Integer, Integer> map = new HashMap<>();\n    for (int i = 0; i < nums.length; i++) {\n        int comp = target - nums[i];\n        if (map.containsKey(comp)) return new int[]{map.get(comp), i};\n        map.put(nums[i], i);\n    }\n    return new int[]{};\n}"
}
```

**Submission Statuses:**
| Status | Meaning |
|--------|---------|
| `ACCEPTED` | All test cases passed ✅ |
| `WRONG_ANSWER` | Output doesn't match expected ❌ |
| `PARTIAL` | Some test cases passed |
| `COMPILE_ERROR` | Code failed to compile |
| `RUNTIME_ERROR` | Exception thrown during execution |
| `TIME_LIMIT_EXCEEDED` | Exceeded 5 second limit |

---

### 🏆 Leaderboard & Profile Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/leaderboard` | Public | Full ranked leaderboard |
| GET | `/api/leaderboard/top?n=10` | Public | Top N users |
| GET | `/api/users/me` | User | My profile & stats |
| GET | `/api/users/{id}` | Public | Any user's profile |

---

### 🗄️ H2 Database Console

Access the embedded database at: **http://localhost:8080/h2-console**

```
JDBC URL:  jdbc:h2:file:./codearena-db
Username:  sa
Password:  (leave blank)
```

---

## 🧮 Scoring System

| Difficulty | Points on First Solve |
|-----------|----------------------|
| EASY | 10 pts |
| MEDIUM | 25 pts |
| HARD | 50 pts |

- Points are awarded only **once per problem** (first accepted submission)
- Leaderboard is ranked by **total score**, then by **problems solved**

---

## ✍️ How Code Evaluation Works

1. User submits Java code (method body only — not a full class)
2. The evaluator wraps it in a `Solution` class
3. Compiles it at runtime using `javax.tools.JavaCompiler`
4. Loads the compiled class, invokes the method with each test case input
5. Compares output to expected value
6. Returns pass/fail per test case + overall status

**Supported Language:** Java (Python/JS support can be added via process execution)

---

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
server.port=8080
app.jwt.expiration=86400000       # Token expiry in ms (default: 24h)
app.execution.timeout=5000        # Code execution timeout in ms (default: 5s)
```

---

## 📬 Quick Test with cURL

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@test.com","password":"pass123"}'

# 2. Login → copy the token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pass123"}'

# 3. List problems
curl http://localhost:8080/api/problems

# 4. Submit a solution (replace TOKEN)
curl -X POST http://localhost:8080/api/submissions \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "problemId": 2,
    "language": "JAVA",
    "code": "public int fib(int n) { if(n<=1) return n; return fib(n-1)+fib(n-2); }"
  }'

# 5. Check leaderboard
curl http://localhost:8080/api/leaderboard
```
