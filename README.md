# Finance Data Processing & Access Control Backend

## Project Overview

This project is a **production-grade backend system** for a finance dashboard application. It is designed to manage financial records, enforce role-based access control, and provide analytical insights through aggregated APIs.

The system demonstrates strong backend engineering practices including:

* Clean architecture (Controller → Service → Repository)
* JWT-based authentication
* Role-based authorization (RBAC)
* Data validation and global error handling
* Pagination, filtering, and sorting
* Dashboard analytics
* Comprehensive test coverage (unit, integration, security)

---

## Objective

To build a scalable and maintainable backend that:

* Manages users and roles securely
* Handles financial transactions efficiently
* Provides analytical insights for dashboards
* Enforces strict access control
* Demonstrates real-world backend design patterns

---

## Tech Stack

| Layer      | Technology              |
| ---------- | ----------------------- |
| Language   | Java 17                 |
| Framework  | Spring Boot 3           |
| Security   | Spring Security + JWT   |
| Database   | PostgreSQL              |
| ORM        | Spring Data JPA         |
| Build Tool | Maven                   |
| API Docs   | Swagger (OpenAPI)       |
| Testing    | JUnit, Mockito, MockMvc |

---

##Project Modules

---

### 1. Authentication & Authorization Module

**Features:**

* User login with JWT token generation
* Stateless authentication
* Token validation for every request
* Role-based access enforcement

**Key Components:**

* `AuthController`
* `AuthService`
* `JwtUtil`
* `JwtAuthenticationFilter`
* `SecurityConfig`

---

### 2. User Management Module

**Features:**

* Create users
* Assign roles (ADMIN, ANALYST, VIEWER)
* Activate / deactivate users
* Fetch user details

**Access Control:**

* Only ADMIN can create/update users

---

### 3. Financial Records Module

**Features:**

* Create, update, delete financial records
* Filter by:

  * Date range
  * Category
  * Transaction type
* Pagination & sorting support

**Transaction Types:**

* INCOME
* EXPENSE

---

### 4. Dashboard & Analytics Module

**Features:**

* Total Income
* Total Expense
* Net Balance
* Category-wise aggregation
* Monthly trends
* Weekly trends
* Recent transactions

---

### 5. Access Control Module

**Roles & Permissions:**

| Role    | Permissions                            |
| ------- | -------------------------------------- |
| VIEWER  | Read-only access (dashboard + records) |
| ANALYST | Read + analytics                       |
| ADMIN   | Full system access                     |

**Implementation:**

* JWT authentication
* Spring Security filters
* `@PreAuthorize` annotations

---

### 6. Validation & Error Handling Module

**Features:**

* Input validation using `@Valid`, `@NotNull`, etc.
* Global exception handling (`@RestControllerAdvice`)
* Standardized error responses

---

## Database Design

### User Entity

* id
* name
* email (unique)
* password (BCrypt encoded)
* role
* status
* createdAt
* updatedAt

---

### FinancialRecord Entity

* id
* amount
* type (INCOME / EXPENSE)
* category
* date
* description
* createdBy
* createdAt
* updatedAt

---

## Security Design

* Stateless authentication using JWT
* Token validation on every request
* Role-based endpoint protection
* Method-level authorization using `@PreAuthorize`

---

## API Highlights

### Auth APIs

* `POST /api/auth/login`

---

### User APIs

* `POST /api/users`
* `GET /api/users`
* `GET /api/users/{id}`
* `PUT /api/users/{id}/role`
* `PATCH /api/users/{id}/status`

---

### Financial APIs

* `POST /api/records`
* `GET /api/records`
* `GET /api/records/filter`
* `PUT /api/records/{id}`
* `DELETE /api/records/{id}`

---

### Dashboard APIs

* `GET /api/dashboard/summary`
* `GET /api/dashboard/category-summary`
* `GET /api/dashboard/trends`
* `GET /api/dashboard/trends/weekly`
* `GET /api/dashboard/recent`

---

##  API Documentation

Swagger UI available at:

```text
http://localhost:8080/swagger-ui/index.html
```

---

##  Setup Instructions

### 1. Clone Repository

```bash
git clone <repo-url>
cd finance-app
```

---

### 2. Configure Database

Update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/financeapp
spring.datasource.username=postgres
spring.datasource.password=your_password
```

---

### 3. Run Application

```bash
mvn spring-boot:run
```

---

##  Testing

###  Test Coverage

| Type              | Count        |
| ----------------- | ------------ |
| Unit Tests        | 18           |
| Integration Tests | 22           |
| Security Tests    | 18           |
| **Total**         | **58 tests** |

---

### Testing Scope

* Business logic validation
* API integration flow
* Pagination & filtering
* Security (JWT, roles, invalid tokens)
* End-to-end scenarios

---

## Key Features Implemented

* Clean layered architecture
* DTO-based design
* Secure authentication system
* Role-based authorization
* Pagination + filtering + sorting
* Aggregation queries for analytics
* Global exception handling
* Swagger documentation
* Comprehensive test suite

---

## Design Decisions & Assumptions

* JWT used for stateless scalability
* Roles implemented using ENUM for simplicity
* Pagination added for performance optimization
* Aggregation handled at database level for efficiency
* DTOs used to avoid exposing entities

---

## Highlights

* Production-level backend architecture
* Strong focus on security and validation
* Real-world financial analytics implementation
* Extensive automated testing (including security testing)

---

## Future Enhancements

* Refresh token mechanism
* Rate limiting
* Caching (Redis)
* Multi-user financial ownership
* Export reports (PDF/CSV)

---

## Conclusion

This project demonstrates:

* Strong backend development skills
* Clean architecture and code organization
* Real-world problem solving
* Security-first design
* Production-ready system thinking

---
