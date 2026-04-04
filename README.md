# Finance Data Processing & Access Control Backend

A production-grade RESTful backend system for secure financial record management and user administration. Built using Spring Boot with JWT authentication and role-based access control (RBAC), following clean architecture principles for scalability and maintainability.

---

## 📋 Table of Contents

* Technology Stack
* Architecture & Design
* Core Features
* Project Structure
* Quick Start
* API Overview
* Security Implementation
* Database Design
* Testing
* Key Highlights

---

## 🛠 Technology Stack

| Layer      | Technology              |
| ---------- | ----------------------- |
| Language   | Java 17                 |
| Framework  | Spring Boot 3           |
| Security   | Spring Security + JWT   |
| Database   | PostgreSQL              |
| ORM        | Spring Data JPA         |
| Build Tool | Maven                   |
| Docs       | Swagger (OpenAPI)       |
| Testing    | JUnit, Mockito, MockMvc |

---

## 🏗 Architecture & Design

The application follows a **layered architecture**:

Request → Controller → Service → Repository → Database

### Key Principles

* Separation of concerns (Controller / Service / Repository)
* DTO-based communication
* Global exception handling
* Stateless authentication using JWT
* Role-based authorization using annotations

---

## 🚀 Core Features

### 🔐 Authentication & Authorization

* JWT-based login system
* Stateless session management
* Role-based access control (ADMIN, ANALYST, VIEWER)

### 👤 User Management

* Create and manage users
* Role assignment and status control
* Admin-restricted operations

### 💰 Financial Records

* CRUD operations for transactions
* Filtering (date, category, type)
* Pagination and sorting support

### 📊 Dashboard & Analytics

* Total income & expense
* Net balance calculation
* Category-wise aggregation
* Trends (monthly & weekly)
* Recent transactions

### ⚠️ Validation & Error Handling

* Input validation using annotations
* Centralized exception handling
* Standardized API error responses

---

## 📁 Project Structure

```
controller/   → REST endpoints  
service/      → Business logic  
repository/   → Data access  
entity/       → Database models  
dto/          → Request/response objects  
security/     → JWT & authentication  
exception/    → Error handling  
util/         → Mappers & helpers  
```

---

## 🚀 Quick Start

### Prerequisites

* Java 17+
* Maven
* PostgreSQL

### Setup

```bash
git clone <repo-url>
cd financeapp
```

### Configure Database

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/financeapp
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### Run Application

```bash
mvn spring-boot:run
```

### Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

---

## 📡 API Overview

### Auth

* POST /api/auth/login

### Users (Admin Only)

* POST /api/users
* GET /api/users
* PUT /api/users/{id}/role
* PATCH /api/users/{id}/status

### Financial Records

* POST /api/records
* GET /api/records
* PUT /api/records/{id}
* DELETE /api/records/{id}

### Dashboard

* GET /api/dashboard/summary
* GET /api/dashboard/trends
* GET /api/dashboard/category-summary

---

## 🔐 Security Implementation

* JWT authentication (stateless)
* Password hashing using BCrypt
* Role-based endpoint protection
* Method-level authorization using `@PreAuthorize`

### Roles

| Role    | Access Level     |
| ------- | ---------------- |
| VIEWER  | Read-only        |
| ANALYST | Read + analytics |
| ADMIN   | Full access      |

---

## 💾 Database Design

### User

* id, name, email, password
* role, status
* createdAt, updatedAt

### FinancialRecord

* id, amount, type
* category, date, description
* createdBy, timestamps

---

## 🧪 Testing

### Coverage

* Unit Tests
* Integration Tests
* Security Tests

### Scope

* Business logic validation
* API workflows
* Authentication & authorization
* Filtering and pagination

---

## 🌟 Key Highlights

* Clean layered architecture
* Production-ready security design
* Role-based access control (RBAC)
* Scalable and maintainable codebase
* Comprehensive testing strategy
* Real-world financial analytics implementation

---

## 🔮 Future Enhancements

* Refresh token mechanism
* Rate limiting
* Redis caching
* Multi-user financial ownership
* Export reports (PDF/CSV)

---

## 📌 Conclusion

This project demonstrates strong backend engineering skills with a focus on:

* Security-first design
* Clean architecture
* Real-world problem solving
* Scalable system development

---
