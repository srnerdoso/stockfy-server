# Tests

## Testing Framework
- Framework: JUnit 5 + Mockito + Spring Boot Test + Testcontainers.
- Approach: **TDD mandatory** (write tests before implementation).

## Coverage by Layer
- **Domain**: Pure unit tests (no Spring, no heavy mocks).
- **Application**: Unit tests with mocks (Mockito).
- **Infrastructure**: Integration tests (Testcontainers + real PostgreSQL).
- **Presentation**: API tests (MockMvc or WebTestClient).

## API Integration Test Rules
- **Mandatory:** Every endpoint must have complete coverage.
- **Scenarios:** Must cover all success and error paths (e.g., 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 429 Too Many Requests, 500 Internal Server Error).
- **Isolation:** Tests must be deterministic and independent.

## General Test Rules
- Every new behavior must have a test.
- Every business rule must be covered by domain tests.
- Do not test getters/setters or trivial code.
- Tests must be independent and deterministic.
- Do not share state between tests.
- Test names must describe the expected behavior.
- Do not use generic or sequential display names (e.g., "Step 1", "Step 2"); @DisplayName must clearly describe the test behavior in pt-BR.
- Tests are immutable by default:
    - Do not modify existing tests.
    - New behaviors require new tests.
- Exception: Tests may be changed only if they are incorrect, inconsistent, or obsolete with the domain, **or** if the behavior change was intentional and explicit.

## Security Rule
- Never modify tests just to make code pass.
- If a test fails, first assume the code is wrong.

## Test Priority and Naming
- **Priority:** Tests define the expected behavior (source of truth).
- **Naming Pattern:**
  `ClassNameTest`  
  `method_whenCondition_thenExpectedResult`
- **Structure:** `src/test/java/...` mirrors `src/main/java/...` structure.

## Mocks and Integration
- **Mocks:**
    - Use only in the Application layer.
    - Never mock the domain.
    - Prefer real objects whenever possible.
- **Integration:**
    - Use Testcontainers for PostgreSQL.
    - Do not use in-memory databases (e.g., H2).

## Events and Performance
- **Events:**
    - Test publishing and consumption separately.
    - Validate side effects, not internal implementation.
- **Performance:**
    - Unit tests must be fast.
    - Integration tests must be isolated and controlled.

## Test Data and Failure Scenarios
- **Test Data:**
    - Use factories/builders for test objects.
    - Avoid duplicated setup.
- **Failures:**
    - Tests must cover all error and exception scenarios.

## Rate Limit Tests
- Test allowed request limit within the configured window.
- Test blocking when limit is exceeded (HTTP 429).
- Test limit reset after window expiration.
- Validate behavior per identity (IP, user, or token).
- Ensure different identities do not share limits.