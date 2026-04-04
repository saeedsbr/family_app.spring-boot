# Life Pulse Backend: Personal Management System API

The **Life Pulse Backend** is a Spring Boot application serving as the central API for the ecosystem.

### Core Modules
- **Vehicle Hub**: Fleet management, fuel logging, and maintenance tracking.
- **Meter Pro**: Utility (electricity, gas, water) consumption tracking.
- **Budget Tracker**: Financial management with transactions, categories, and sharing.
- **Collaborative Access**: Granular control for family sharing across all modules.

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.4
- **Language**: Java 17
- **Database**: H2 (Development), with PostgreSQL support for production.
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security with stateless JWT Authentication.
- **Validation**: Spring Boot Starter Validation.
- **Mail**: Spring Boot Starter Mail for password recovery.

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+

### Running Locally

1. **Clone the repository** (if not already done).
2. **Configure Database**: The app uses an H2 file-based database (`vmsdb.mv.db`) by default for development.
3. **Run the application**:
   ```bash
   mvn spring-boot:run -Dmaven.test.skip=true
   ```
   > [!NOTE]
   > We use `-Dmaven.test.skip=true` to bypass legacy test package issues during initial setup.

The API will be available at `http://localhost:8080`.

## 📂 Project Structure

- `com.lifepulse.controller`: REST API endpoints.
- `com.lifepulse.service`: Business logic layer.
- `com.lifepulse.entity`: JPA entities.
- `com.lifepulse.repository`: Spring Data JPA repositories.
- `com.lifepulse.security`: JWT and Security configuration.
- `com.lifepulse.dto`: Data Transfer Objects for API requests and responses.

## 🔑 Authentication

The API uses JWT-based authentication. Most endpoints require an `Authorization: Bearer <token>` header.

- **Login**: `POST /api/auth/signin`
- **Register**: `POST /api/auth/signup`

## 👤 User Profile & Preferences

The API supports managing user identity and global preferences:

- **Update Profile**: `PUT /api/users/profile`
    - **Fields**: `name`, `currency`, `logoUrl`.
    - **Description**: Updates the user's display name, preferred currency, and avatar URL. These changes are reflected across all family management modules.

## 📖 API Documentation

For a full list of endpoints and data models, refer to the [API Reference](../vehicle%20system%20next%20js/doc/API_Reference.md) in the web module.

---
*Developed by the Life Pulse Backend Team*
