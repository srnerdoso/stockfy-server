# Internationalization

## Overview

Internationalization (i18n) ensures that all user-facing content is adaptable to different languages and locales by **separating text from source code**.

All messages returned by the API must be externalized and resolved dynamically based on the user's locale.

---

## Core Principle

The agent **must never use hardcoded strings** in any API response.

This includes:

- Error messages
- Validation messages
- Success messages
- Any user-facing text

All user-visible content must be retrieved from a centralized message system.

---

## Message Resolution

All messages must be resolved using **Spring's `MessageSource`**.

### Rules

- Always retrieve messages via `MessageSource`
- Never embed literal strings directly in code
- Always use message keys
- Support locale-based resolution
- Provide fallback behavior when a key is missing

---

## Message Structure

Messages must be defined in resource bundle files.

### Example

```
messages.properties
messages_pt_BR.properties
messages_en_US.properties

```

Each file represents a locale-specific message set.

---

## Message Keys

### Guidelines

- Use descriptive and consistent naming
- Avoid abbreviations
- Group keys by domain or feature

### Example

```
user.not.found=User not found
auth.invalid.credentials=Invalid credentials
product.created.success=Product created successfully
```

---

## Parameterized Messages

Messages must support dynamic values through parameters.

### Example

```
user.created=User {0} created successfully

```

- Parameters must be passed through `MessageSource`
- Avoid string concatenation in code

---

## What Must Be Internationalized

All user-facing content must be externalized:

- API responses
- Exception messages returned to clients
- Validation errors
- Success responses

### Do NOT internationalize:

- Internal logs
- Debug messages
- Developer-only messages

---

## Anti-Patterns (Forbidden)

The agent must never:

- Hardcode strings in controllers or services
- Concatenate strings to build messages
- Return raw exception messages
- Use untranslated default values in production

---

## Consistency Rules

- Use the same message key for the same meaning across the system
- Avoid duplicate or similar keys
- Maintain a single source of truth for each message

---

## Goal

Ensure:

- Full separation between code and text
- Multi-language support
- Consistent messaging across the application
- Easy maintenance and scalability

This guarantees proper internationalization and a consistent user experience across different locales.
