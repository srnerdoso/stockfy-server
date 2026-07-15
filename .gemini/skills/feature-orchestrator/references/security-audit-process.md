# Security Reference: Audit Process

This document defines the structured security audit process for evaluating features and code. It is a strictly *
*read-only** protocol designed to identify, analyze, and report vulnerabilities without modifying the source material.

---

## Core Mandate

* **Read-Only Execution**: The auditor must not modify code or apply fixes.
* **Objective**: Detect vulnerabilities based on OWASP, CWE, and real-world patterns in both AI-generated and
  human-written code.

---

## Audit Workflow

### 1. Scope Identification

Determine the audit boundaries, focusing on authentication, authorization, data handling, external integrations, and
sensitive operations.

### 2. Secrets and Sensitive Data

Scan source code, configuration files, and logs for:

* Hardcoded credentials (API keys, passwords, tokens).
* **Severity**: HIGH.
* **Report**: Exact file and line location with exposure risk description.

### 3. Input Validation and Injection

Analyze all external inputs (Request bodies, query parameters, headers, file uploads) for:

* Missing validation or sanitization.
* Direct usage in SQL queries, system commands, or HTML rendering.
* Vulnerabilities: SQL Injection, XSS, Command Injection.

### 4. Access Control

* **Authentication**: Ensure identity verification is required for all protected resources.
* **Authorization**: Validate role enforcement and check for Insecure Direct Object Reference (IDOR).

### 5. Dependency Audit

Inspect libraries and packages for:

* Outdated versions.
* Known vulnerabilities.
* Non-existent or hallucinated dependencies.

### 6. Dangerous Patterns and APIs

Identify risky execution methods:

* Dynamic execution (eval).
* Reflection misuse.
* Unsafe deserialization.
* Direct system calls.

### 7. Cryptography and Data Protection

Verify the use of modern cryptographic standards:

* Flag weak algorithms (MD5, SHA1).
* Identify improper hashing or insecure token handling.
* Detect exposure of sensitive data.

### 8. Security Configurations

Validate environment and protocol controls:

* CORS policies (Ensure not set to "*").
* Presence of security headers.
* Rate limiting implementation.
* Secure default settings.

### 9. Logging and Error Handling

Ensure logs and error messages do not leak:

* Tokens, passwords, or PII.
* Verbose system information.

### 10. Business Logic

Analyze the application flow for:

* Broken logical sequences.
* Privilege escalation paths.
* Missing validation within business rules.

---

## Reporting Standards

### Risk Classification

Every finding must include:

* **Severity**: HIGH, MEDIUM, or LOW.
* **Impact**: Technical and business consequences.
* **Attack Scenario**: Practical exploit path.

### Output Format

The output must contain **only** findings. No code patches or fixes are permitted.

**Template:**
[SEVERITY] Issue Title  
Location: file:line  
Description:  
Impact:  
Attack Scenario:

---

## Operational Rules

* Maintain a strictly read-only stance; never include code patches.
* Assume code is insecure by default.
* Prioritize real-world exploitability over theoretical risks.
* Maintain technical and objective language throughout the report.