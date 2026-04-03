# Finance App — Spring Boot Backend

## Stack
- Java 17, Spring Boot 3.2, Spring Security 6
- PostgreSQL, Spring Data JPA
- JWT (jjwt 0.11.5), BCrypt, Lombok

---

## Project Structure

```
src/main/java/com/financeapp/
├── FinanceAppApplication.java
│
├── config/
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
│
├── controller/
│   ├── AuthController.java
│   ├── FinancialRecordController.java
│   └── UserController.java
│
├── dto/
│   ├── AuthResponse.java
│   ├── FinancialRecordRequest.java
│   ├── FinancialRecordResponse.java
│   ├── LoginRequest.java
│   ├── RoleUpdateRequest.java
│   ├── StatusUpdateRequest.java
│   ├── UserRequest.java
│   └── UserResponse.java
│
├── entity/
│   ├── FinancialRecord.java
│   ├── Role.java              (VIEWER | ANALYST | ADMIN)
│   ├── Status.java            (ACTIVE | INACTIVE)
│   ├── TransactionType.java
│   └── User.java
│
├── exception/
│   ├── DuplicateEmailException.java
│   ├── GlobalExceptionHandler.java
│   ├── InvalidCredentialsException.java
│   ├── ResourceNotFoundException.java
│   └── UserNotFoundException.java
│
├── repository/
│   ├── FinancialRecordRepository.java
│   └── UserRepository.java
│
├── security/
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtUtil.java
│
├── service/
│   ├── AuthService.java
│   ├── FinancialRecordService.java
│   ├── UserService.java
│   └── impl/
│       ├── AuthServiceImpl.java
│       ├── FinancialRecordServiceImpl.java
│       └── UserServiceImpl.java
│
└── util/
    ├── FinancialRecordMapper.java
    └── UserMapper.java
```

---

## Setup

### 1. Create the PostgreSQL database
```sql
CREATE DATABASE financeapp;
```

### 2. Configure environment variables (recommended for production)
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=$(openssl rand -base64 32)
export JWT_EXPIRATION_MS=86400000   # 24 hours
```

### 3. Run
```bash
./mvnw spring-boot:run
```

---

## API Reference

### Authentication

#### POST /api/auth/login

**Request:**
```json
{
  "email": "admin@example.com",
  "password": "admin1234"
}
```

**Response 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6...",
  "role": "ADMIN",
  "username": "Alice Admin"
}
```

**Response 401 — invalid credentials:**
```json
{
  "status": 401,
  "message": "Invalid email or password",
  "timestamp": "2025-04-02T10:00:00"
}
```

**Response 401 — inactive account:**
```json
{
  "status": 401,
  "message": "Account is inactive. Please contact an administrator.",
  "timestamp": "2025-04-02T10:00:00"
}
```

---

### User Management

All user endpoints require `Authorization: Bearer <token>` header.

#### POST /api/users — Admin only

**Request:**
```json
{
  "name": "Bob Analyst",
  "email": "bob@example.com",
  "password": "securepass",
  "role": "ANALYST"
}
```

**Response 201 Created:**
```json
{
  "id": 2,
  "name": "Bob Analyst",
  "email": "bob@example.com",
  "role": "ANALYST",
  "status": "ACTIVE",
  "createdAt": "2025-04-02T10:05:00",
  "updatedAt": "2025-04-02T10:05:00"
}
```

**Response 409 — duplicate email:**
```json
{
  "status": 409,
  "message": "A user with email 'bob@example.com' already exists",
  "timestamp": "2025-04-02T10:05:00"
}
```

---

#### GET /api/users — Admin only

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "name": "Alice Admin",
    "email": "admin@example.com",
    "role": "ADMIN",
    "status": "ACTIVE",
    "createdAt": "2025-04-01T09:00:00",
    "updatedAt": "2025-04-01T09:00:00"
  },
  {
    "id": 2,
    "name": "Bob Analyst",
    "email": "bob@example.com",
    "role": "ANALYST",
    "status": "ACTIVE",
    "createdAt": "2025-04-02T10:05:00",
    "updatedAt": "2025-04-02T10:05:00"
  }
]
```

---

#### GET /api/users/{id} — Admin, Analyst, Viewer

**Response 200 OK:**
```json
{
  "id": 2,
  "name": "Bob Analyst",
  "email": "bob@example.com",
  "role": "ANALYST",
  "status": "ACTIVE",
  "createdAt": "2025-04-02T10:05:00",
  "updatedAt": "2025-04-02T10:05:00"
}
```

**Response 404:**
```json
{
  "status": 404,
  "message": "User not found with id: 99",
  "timestamp": "2025-04-02T10:10:00"
}
```

---

#### PUT /api/users/{id}/role — Admin only

**Request:**
```json
{
  "role": "VIEWER"
}
```

**Response 200 OK:**
```json
{
  "id": 2,
  "name": "Bob Analyst",
  "email": "bob@example.com",
  "role": "VIEWER",
  "status": "ACTIVE",
  "createdAt": "2025-04-02T10:05:00",
  "updatedAt": "2025-04-02T10:15:00"
}
```

---

#### PATCH /api/users/{id}/status — Admin only

**Request:**
```json
{
  "status": "INACTIVE"
}
```

**Response 200 OK:**
```json
{
  "id": 2,
  "name": "Bob Analyst",
  "email": "bob@example.com",
  "role": "VIEWER",
  "status": "INACTIVE",
  "createdAt": "2025-04-02T10:05:00",
  "updatedAt": "2025-04-02T10:20:00"
}
```

---

## Role Permissions Summary

| Endpoint                      | VIEWER | ANALYST | ADMIN |
|-------------------------------|--------|---------|-------|
| POST /api/auth/login          | ✅     | ✅      | ✅    |
| POST /api/users               | ❌     | ❌      | ✅    |
| GET  /api/users               | ❌     | ❌      | ✅    |
| GET  /api/users/{id}          | ✅     | ✅      | ✅    |
| PUT  /api/users/{id}/role     | ❌     | ❌      | ✅    |
| PATCH /api/users/{id}/status  | ❌     | ❌      | ✅    |

---

## Security Notes

- Passwords are hashed with BCrypt (strength 10 by default).
- JWT is signed with HMAC-SHA256. Never commit the secret to source control.
- CSRF is disabled — appropriate for a stateless REST API with JWT.
- Inactive users receive a `401` on login and cannot authenticate.
- Generate a production secret with: `openssl rand -base64 32`
