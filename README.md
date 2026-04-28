# 🏥 CareBridge Backend

CareBridge Backend is a high-performance, developer-centric REST API designed for healthcare facility management. It features a unique **Hybrid Architecture** that combines traditional hand-coded controllers with a **Universal CRUD Framework** for rapid development.

Built with **Java 25**, it leverages the latest language features alongside industry-standard frameworks like **Javalin** and **Hibernate**.

---

## 🚀 Key Features

- **Hybrid API Architecture**: Use traditional DAOs/Controllers for complex business logic, and the Universal CRUD engine for standard data operations.
- **Universal CRUD (v3)**: Automatically generates secure RESTful endpoints for any `@Entity` using reflection.
- **JWT-Based Security**: Robust authentication and Role-Based Access Control (RBAC).
- **Modern Tech Stack**: Java 25, Javalin 6, Hibernate 6.
- **Database Flexibility**: Support for local PostgreSQL, Cloud (Neon.tech), or H2 in-memory for prototyping.
- **Comprehensive Testing**: Integration tests using JUnit 5, Rest-Assured, and Testcontainers.

---

## 🛠 Tech Stack

- **Runtime**: [Java 25](https://openjdk.org/)
- **Web Framework**: [Javalin 6.3.0](https://javalin.io/)
- **ORM**: [Hibernate 6.4.4.Final](https://hibernate.org/orm/)
- **Database**: [PostgreSQL 42.7.3](https://jdbc.postgresql.org/)
- **Security**: JWT (nimbus-jose-jwt) & BCrypt (jbcrypt)
- **JSON Processing**: Jackson 2.16.1
- **Discovery**: Reflections 0.10.2
- **Testing**: JUnit 5, Testcontainers, Rest-Assured

---

## 📦 Project Structure

```text
carebridge-backend/
├── src/main/java/com/carebridge/
│   ├── config/             # Server and Hibernate configuration
│   ├── controllers/        # Hand-coded REST endpoints
│   ├── crud/               # The Universal CRUD Engine (v3)
│   ├── dao/                # Data Access Objects
│   ├── dtos/               # Data Transfer Objects
│   ├── entities/           # JPA Models
│   ├── routes/             # Route definitions
│   ├── UniversalApp.java   # Main entry point (Recommended)
│   └── App.java            # Legacy entry point
├── src/main/resources/     # Configuration files (logback, etc.)
└── pom.xml                 # Maven dependencies and build config
```

---

## ✨ Universal CRUD (v3)

The project includes a "Magical" Universal Server. By simply adding a `@Entity` class to the `entities` package, the system:
1.  Auto-discovers the entity via reflection.
2.  Auto-configures Hibernate schema.
3.  Generates a full suite of REST endpoints under `/api/v3/{resource}`.

**Example Endpoint:**
`GET /api/v3/resident` -> Returns all residents.

---

## 🚦 Quick Start

### 1. Prerequisites
- **JDK 25**
- **Maven 3.9+**
- **PostgreSQL** (Local or [Neon.tech](https://neon.tech))

### 2. Configure Environment
Create a `.env` file in the root directory:
```env
DB_HOST=localhost:5432
DB_NAME=carebridge
DB_USER=postgres
DB_PASSWORD=yourpassword
JWT_SECRET=your_super_secret_key
SERVER_PORT=7070
```

### 3. Build & Run
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.carebridge.UniversalApp"
```

---

## 🧪 Testing

The project uses **Testcontainers** to spin up a real PostgreSQL instance for integration tests.

```bash
mvn test
```

---

## 🛡 Security

Access control is handled via the `AccessController` and `TokenSecurity` classes.
- **Authentication**: `POST /api/auth/login`
- **Authorization**: Role-based checks on endpoints (ADMIN, MANAGER, USER, etc.).

---

## 📖 Detailed Setup
For a step-by-step guide on setting up this project on your own server, see [SETUP.md](./setup.md).
