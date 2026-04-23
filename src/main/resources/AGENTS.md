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
