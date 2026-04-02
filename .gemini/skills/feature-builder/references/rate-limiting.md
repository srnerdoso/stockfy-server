# Rate Limiting Guidelines

## Overview

All endpoints must comply with the application's rate limiting strategy to ensure system stability, fairness, and protection against abuse.

Rate limiting must be implemented using **Bucket4j**, integrated with the application's existing security and request processing flow.

---

## Core Principle

Rate limiting must be **centralized and consistent**.

* Do not implement custom logic outside the established pattern
* Do not duplicate rate limiting behavior across layers

---

## Technology

* Rate limiting must be enforced using **Bucket4j**
* Configuration and behavior must follow the existing project setup

---

## Client Identification

The rate limit key must be derived based on endpoint type:

### Public Endpoints

* Use **hashed IP address**
* Raw IP addresses must never be used
* Hashing must use a secure algorithm (e.g., SHA-256)

### Protected Endpoints

* Use **authenticated user identifier**
* Must rely on the application's security context

---

## Integration

Rate limiting must be applied at the infrastructure level.

### Rules

* Must be implemented via existing **filters or interceptors**
* Must not be implemented inside controllers
* Must not be implemented inside services

### Restrictions

* Do not embed rate limiting logic in business code
* Do not bypass the existing rate limiting layer

### Modification Rule

If implementation requires:

* Changing filters
* Updating interceptors
* Altering rate limiting configuration

The agent must request user permission before proceeding.

---

## Behavior

When the rate limit is exceeded:

* The API must return **HTTP 429 (Too Many Requests)**
* The response must follow the project's standard response structure

---

## Testing Requirements

Integration tests must validate rate limiting behavior.

### Required Scenarios

* Requests within limits must succeed
* Requests exceeding limits must return HTTP 429

### Validation

* Verify response status
* Verify response structure
* Verify message resolution via MessageSource

---

## Anti-Patterns (Forbidden)

The agent must never:

* Use raw IP addresses for rate limiting
* Implement custom rate limiting logic outside Bucket4j
* Place rate limiting logic inside controllers or services
* Bypass filters or interceptors
* Return hardcoded error messages

---

## Goal

Ensure:

* Consistent and centralized rate limiting
* Secure client identification
* Proper integration with application infrastructure
* Reliable and testable behavior
