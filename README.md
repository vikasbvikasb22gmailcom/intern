# 🏥 Hospital Queue Management System

A full-featured **Spring Boot** backend for patient appointment booking and **real-time queue tracking** using JWT authentication, WebSockets, role-based access control, and scheduled reminders.

---

## 📁 Project Structure

```
hospital-queue-system/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/hospital/queue/
    │   │   ├── HospitalQueueApplication.java       ← Main entry point
    │   │   ├── config/
    │   │   │   ├── AppConfig.java                  ← ModelMapper + DB seeder
    │   │   │   ├── OpenApiConfig.java               ← Swagger/OpenAPI setup
    │   │   │   ├── SecurityConfig.java              ← JWT + CORS + role security
    │   │   │   └── WebSocketConfig.java             ← STOMP WebSocket config
    │   │   ├── controller/
    │   │   │   ├── AuthController.java              ← /api/auth/**
    │   │   │   ├── AppointmentController.java       ← /api/appointments/**
    │   │   │   ├── DoctorController.java            ← /api/doctors/**
    │   │   │   ├── AdminController.java             ← /api/admin/**
    │   │   │   ├── NotificationController.java      ← /api/notifications/**
    │   │   │   ├── UserProfileController.java       ← /api/profile/**
    │   │   │   └── QueueWebSocketController.java    ← WebSocket /app/queue/**
    │   │   ├── dto/                                 ← Request/Response DTOs
    │   │   ├── entity/                              ← JPA Entities
    │   │   ├── enums/                               ← Role, Status, DayOfWeek
    │   │   ├── exception/                           ← Global exception handling
    │   │   ├── repository/                          ← Spring Data JPA repos
    │   │   ├── scheduler/                           ← Reminder + no-show jobs
    │   │   ├── security/                            ← JWT filter, UserDetails
    │   │   └── service/                             ← Business logic
    │   └── resources/
    │       └── application.properties
    └── test/
```

---

## ⚙️ Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 17                             |
| Framework    | Spring Boot 3.2                     |
| Security     | Spring Security + JWT (jjwt 0.11)   |
| Database     | H2 (dev) / MySQL 8 (prod)           |
| ORM          | Spring Data JPA / Hibernate         |
| Real-time    | WebSocket + STOMP (SockJS)          |
| Email        | Spring Mail (SMTP simulation)       |
| Scheduler    | Spring @Scheduled                   |
| API Docs     | SpringDoc OpenAPI (Swagger UI)      |
| Build        | Maven                               |

---

## 🚀 Step-by-Step Setup & Run

### ✅ Prerequisites

- **Java 17+** → [Download](https://adoptium.net/)
- **Maven 3.8+** → [Download](https://maven.apache.org/)
- **MySQL 8** (optional, H2 in-memory is used by default)
- An IDE like IntelliJ IDEA or VS Code

---

### Step 1 — Clone or Extract the Project

```bash
unzip hospital-queue-system.zip
cd hospital-queue-system
```

---

### Step 2 — Configure the Database

#### Option A: H2 In-Memory (No setup needed — works out of the box)

The default `application.properties` already uses H2.
> ⚠️ Data is lost when the app restarts.

#### Option B: MySQL (Recommended for production)

1. Create a MySQL database:
```sql
CREATE DATABASE hospital_queue_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'hospital'@'localhost' IDENTIFIED BY 'yourpassword';
GRANT ALL PRIVILEGES ON hospital_queue_db.* TO 'hospital'@'localhost';
FLUSH PRIVILEGES;
```

2. Edit `src/main/resources/application.properties`:
```properties
# Comment out H2 lines and uncomment MySQL lines:
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_queue_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=hospital
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

---

### Step 3 — Configure Email (Optional)

To enable real email sending, edit `application.properties`:

```properties
spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-app-password   # Gmail App Password (not login password)
```

> 💡 By default, emails are **simulated** (logged to console). No SMTP setup needed to run.

---

### Step 4 — Build the Project

```bash
mvn clean install -DskipTests
```

---

### Step 5 — Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/hospital-queue-system-1.0.0.jar
```

The server starts at: **http://localhost:8080**

---

### Step 6 — Open Swagger UI

Visit: **http://localhost:8080/swagger-ui.html**

You'll see all endpoints grouped by:
- 🔐 Authentication
- 👨‍⚕️ Doctors
- 📅 Appointments
- 🛡️ Admin
- 🔔 Notifications
- 👤 Profile

---

### Step 7 — H2 Console (dev only)

Visit: **http://localhost:8080/h2-console**

- JDBC URL: `jdbc:h2:mem:hospitaldb`
- Username: `sa`
- Password: *(leave blank)*

---

## 🔑 Default Admin Credentials

```
Email:    admin@hospital.com
Password: Admin@123
```

Created automatically on first startup.

---

## 📡 API Quick Reference

### Authentication

| Method | Endpoint             | Description              | Auth Required |
|--------|----------------------|--------------------------|---------------|
| POST   | /api/auth/register   | Register new patient     | No            |
| POST   | /api/auth/login      | Login → get JWT token    | No            |
| POST   | /api/auth/refresh-token | Refresh JWT token     | No            |

### Doctors

| Method | Endpoint                        | Role         |
|--------|---------------------------------|--------------|
| GET    | /api/doctors                    | Public       |
| GET    | /api/doctors/{id}               | Public       |
| GET    | /api/doctors/specializations    | Public       |
| GET    | /api/doctors/{id}/schedule      | Public       |
| POST   | /api/doctors                    | ADMIN        |
| PUT    | /api/doctors/{id}               | ADMIN/DOCTOR |
| POST   | /api/doctors/{id}/schedule      | ADMIN/DOCTOR |
| DELETE | /api/doctors/schedule/{id}      | ADMIN/DOCTOR |

### Appointments

| Method | Endpoint                              | Role           |
|--------|---------------------------------------|----------------|
| POST   | /api/appointments                     | PATIENT        |
| GET    | /api/appointments/my                  | PATIENT        |
| GET    | /api/appointments/available-slots     | Authenticated  |
| GET    | /api/appointments/{id}                | Owner/Doctor   |
| GET    | /api/appointments/{id}/queue-status   | Owner/Doctor   |
| GET    | /api/appointments/queue/doctor/{id}   | Authenticated  |
| PATCH  | /api/appointments/{id}/cancel         | Owner/Doctor   |
| PATCH  | /api/appointments/{id}/reschedule     | PATIENT        |
| PATCH  | /api/appointments/{id}/status         | DOCTOR/ADMIN   |
| GET    | /api/appointments                     | ADMIN          |
| GET    | /api/appointments/doctor/{id}         | DOCTOR/ADMIN   |

### Admin

| Method | Endpoint                          | Role  |
|--------|-----------------------------------|-------|
| GET    | /api/admin/dashboard              | ADMIN |
| GET    | /api/admin/patients               | ADMIN |
| GET    | /api/admin/patients/{id}          | ADMIN |
| PATCH  | /api/admin/users/{id}/toggle-status | ADMIN |
| DELETE | /api/admin/users/{id}             | ADMIN |

---

## 🔄 Real-Time Queue via WebSocket

Connect using SockJS + STOMP:

```javascript
// Include in your frontend:
// <script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
// <script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>

var socket = new SockJS('http://localhost:8080/ws');
var stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);

    // Subscribe to doctor #1's queue updates
    stompClient.subscribe('/topic/queue/1', function(message) {
        var queue = JSON.parse(message.body);
        console.log('Queue update:', queue);
        // queue = array of QueueStatusResponse objects
    });

    // Request current queue
    stompClient.send('/app/queue/1', {}, '');
});
```

Queue updates are automatically broadcast whenever:
- A new appointment is booked
- An appointment is cancelled or rescheduled
- A doctor updates appointment status

---

## ⏰ Scheduled Jobs

| Job                       | Schedule             | Description                              |
|---------------------------|----------------------|------------------------------------------|
| Appointment Reminders     | Every 5 minutes      | Sends email/SMS reminder 30 min before   |
| No-Show Marker            | Daily at midnight    | Marks unattended appointments as NO_SHOW |
| Hourly Stats Logger       | Every hour           | Logs daily appointment counts            |

---

## 🧪 Running Tests

```bash
mvn test
```

Tests use H2 in-memory database automatically (`src/test/resources/application.properties`).

---

## 🔐 JWT Usage

1. Login at `POST /api/auth/login` → copy `accessToken`
2. In Swagger UI → click **Authorize** → paste `<your_token>`
3. In Postman/curl:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Token expires in **24 hours**. Use `POST /api/auth/refresh-token` to renew.

---

## 📝 Sample API Calls (curl)

### Register a patient
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Raj",
    "lastName": "Kumar",
    "email": "raj@example.com",
    "phone": "9876543210",
    "password": "password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@hospital.com","password":"Admin@123"}'
```

### Create a doctor (Admin)
```bash
curl -X POST http://localhost:8080/api/doctors \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Priya",
    "lastName": "Sharma",
    "email": "dr.priya@hospital.com",
    "phone": "9123456789",
    "password": "doctor123",
    "specialization": "Cardiology",
    "qualification": "MBBS, MD",
    "consultationFee": 800,
    "experienceYears": 10,
    "maxPatientsPerDay": 20
  }'
```

### Add doctor schedule
```bash
curl -X POST http://localhost:8080/api/doctors/1/schedule \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "dayOfWeek": "MONDAY",
    "startTime": "09:00",
    "endTime": "17:00",
    "isActive": true
  }'
```

### Book an appointment (Patient)
```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Authorization: Bearer <PATIENT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": 1,
    "appointmentDate": "2026-04-01",
    "appointmentTime": "10:00",
    "symptoms": "Chest pain and shortness of breath"
  }'
```

### Get queue status
```bash
curl http://localhost:8080/api/appointments/1/queue-status \
  -H "Authorization: Bearer <PATIENT_TOKEN>"
```

---

## 🛠️ Troubleshooting

| Issue                          | Solution                                                  |
|--------------------------------|-----------------------------------------------------------|
| Port 8080 already in use       | Change `server.port=8081` in application.properties       |
| H2 console shows no tables     | Ensure DDL auto is `create-drop`, check URL matches       |
| JWT Invalid token error        | Re-login and use fresh token; check secret key length     |
| Email not sending              | Check SMTP credentials; emails are logged by default      |
| MySQL connection refused       | Ensure MySQL is running and credentials are correct       |
| WebSocket not connecting       | Check CORS config and ensure /ws endpoint is not blocked  |

---

## 🧩 Key Design Patterns Used

- **Repository Pattern** — Spring Data JPA abstractions
- **Service Layer** — Business logic separated from controllers
- **DTO Pattern** — Clean separation of API contracts from entities
- **Builder Pattern** — Lombok `@Builder` on all entities/DTOs
- **Filter Chain** — JWT authentication via `OncePerRequestFilter`
- **Observer Pattern** — WebSocket broadcasts on state changes
- **Scheduler Pattern** — `@Scheduled` for background jobs

---

*Built with ❤️ using Spring Boot 3.2 + Java 17*
