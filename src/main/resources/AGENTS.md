# Resources

## Directory Structure

```txt
resources
├───db
│   └───migration               # Migrations
├───static                      # Static files
└───templates                   # Templates
└───application.properties      # Default configuration file
└───application-dev.properties  # Development configuration file
└───application-prod.properties # Production configuration file
└───messages.properties         # Internationalization messages
```

## Application Config

### application.properties

Defines the default application configuration for all profiles.

- **NEVER USE** a profile other than `prod` as a fallback in `application.properties`.

### application-dev.properties

Defines development configurations.
Rules for this file are more flexible and focused on development.

### application-prod.properties

Defines production configurations.
Rules in this file are strict and focused on production.

- **NEVER USE** hardcoded credential values in `application-prod.properties`.

## Internationalization Messages

### Key Structure: `namespace.property.modifier`

| Category        | Pattern                      | Examples                                                 |
|:----------------|:-----------------------------|:---------------------------------------------------------|
| **Entities**    | `entity.property`            | `user.name`, `product.price`                             |
| **Validation**  | `entity.property.constraint` | `user.email.not-empty`, `order.total.min`                |
| **Actions**     | `action.verb`                | `action.save`, `action.delete`                           |
| **Feedback**    | `feedback.type.outcome`      | `feedback.success.register`, `feedback.error.connection` |
| **Exceptions**  | `exception.type`             | `exception.resource-not-found`                           |

### Formatting Rules

* **Casing:** Strict `kebab-case` for all keys.
* **Delimiter:** Use **dot notation** (`.`) to represent object hierarchy.
* **Uniqueness:** Keys must be descriptive enough to avoid namespace collisions.
