# Users Module - Context & Rules

## Core Responsibilities
- Identity and Access Management (IAM): Authentication, Authorization, Auditing, Rate Limiting.

## Technical Stack & Decisions
- **Authentication:** JWT (HttpOnly Cookies), Redis for Refresh Tokens.
- **Authorization:** RBAC (ADMIN, USER) with granular permissions.
- **Auditing:** Hibernate Envers + AuditorAware.
- **Rate Limiting:** Bucket4j (Login: 5-15 req/min; General API: 10 req/min).
- **Data Integrity:** Soft delete (`active` field) mandatory for entities. Permanent deletion prohibited.
- **Eventing:** Security alerts via RabbitMQ (EDA).

## Module Standards
- **Security:**
    - Passwords must **never** be returned in DTOs or used in logs.
    - User IDs *may* be returned in DTOs only when explicitly requested by the user.
- **Auditing:** All service-level mutations must be audited.
- **Rate Limiting:** Validate rate limits in integration tests.
- **Entity Design:** All entities must implement the `active` field for soft delete.

## API Endpoints

### Authentication (`/api/v1/auth/sessions`)
- `POST /`: Login user. Request: `LoginRequest`. Response: `AuthResponse` (Factory pattern used for DTOs). Sets HttpOnly cookies.
- `POST /refresh`: Refresh JWT tokens. Request: `refreshToken` (cookie). Response: `AuthResponse` (Factory pattern used for DTOs). Sets HttpOnly cookies.
- `DELETE /current`: Logout user. Request: `refreshToken` (cookie). Clears HttpOnly cookies.

### User Management (`/api/v1/users`)
- `POST /`: Register a new user. Request: `RegisterRequest`. Response: `Void` (HTTP 201 Created). Requires ADMIN role.
- `POST /{id}/password-reset-codes`: Generate password reset code for a user. Request: `id` (path). Response: `ResetCodeResponse` (code). Requires ADMIN role.
- `PATCH /password`: Reset user password using code. Request: `ResetPasswordRequest`. Response: `Void`.
- `GET /me`: Retrieve the current authenticated user's profile. Returns `UserResponse` (Factory pattern used for DTOs).
- `GET /{id}`: Retrieve a specific user by ID. User ID is returned in DTO only if explicitly requested. Returns `UserResponse` (Factory pattern used for DTOs).
- `GET /`: Find all users, optionally filtered by name. Returns `List<UserResponse>` (Factory pattern used for DTOs). Requires ADMIN role.
- `PATCH /me`: Update the current authenticated user's profile. Request: `UpdateProfileRequest`. Response: `Void`.
- `DELETE /{id}`: Soft delete a user by ID. Response: `Void`. Requires ADMIN role.
