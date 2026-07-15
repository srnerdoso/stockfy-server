# OpenAPI Documentation Guidelines

## Overview

All API endpoints must be documented using **Swagger OpenAPI** through dedicated documentation interfaces. This ensures a clear separation between documentation and implementation, improving readability and maintainability.

---

## Core Principle

Documentation must be **decoupled from business logic**.

* Controllers must not contain OpenAPI annotations
* Documentation must reside in dedicated interfaces

---

## Documentation Interface

Each resource must have a corresponding documentation interface responsible exclusively for API documentation.

### Rules

* The interface is responsible **only for endpoint documentation**
* Controllers must **extend the corresponding documentation interface**
* No business logic is allowed in the documentation interface

---

## Naming Convention

Documentation interfaces must follow this pattern:

```
<Resource>ControllerDoc
```

### Examples

* `AuthControllerDoc`
* `UserControllerDoc`
* `ProductControllerDoc`

---

## Responsibilities

The documentation interface must include all OpenAPI-related annotations:

* `@Operation`
* `@ApiResponse`
* `@Parameter`
* `@RequestBody`
* `@Schema`
* Any other relevant OpenAPI annotations

Controllers must:

* Extend the documentation interface
* Implement only endpoint logic
* Avoid any OpenAPI annotations

---

## Example Structure

### Documentation Interface

```java
public interface UserControllerDoc {

    @Operation(summary = "Create a new user")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    ResponseEntity<UserResponse> createUser(UserRequest request);
}
```

### Controller Implementation

```java
@RestController
@RequestMapping("/users")
public class UserController implements UserControllerDoc {

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest request) {
        // business logic only
    }
}
```

---

## Separation of Concerns

This approach enforces:

* Clear separation between **documentation** and **implementation**
* Cleaner and more readable controllers
* Easier maintenance of API documentation
* Centralized control of OpenAPI definitions

---

## Anti-Patterns (Forbidden)

The agent must never:

* Place OpenAPI annotations directly in controllers
* Mix documentation with business logic
* Duplicate documentation across multiple classes
* Create documentation outside the defined interface pattern

---

## Goal

Ensure:

* Consistent API documentation structure
* Maintainable and scalable codebase
* Clear separation of responsibilities
* Standardized OpenAPI usage across all endpoints
