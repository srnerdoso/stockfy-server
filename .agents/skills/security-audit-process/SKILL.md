---
name: security-audit-process
description:
  Use this skill to perform a structured security audit on newly implemented
  features or existing code. It detects common vulnerabilities in AI-generated
  and human-written code based on OWASP, CWE, and real-world patterns.
---

# Security Audit Process

This skill performs a STRICT security audit.

It is READ-ONLY.

The agent MUST NOT:
- Modify code
- Apply fixes

The agent MUST:
- Identify, analyze, and report vulnerabilities only

---

## Workflow

### 1. Identify Scope

Determine what will be audited:
- New feature
- Modified code
- Existing module

Focus areas:
- Authentication
- Authorization
- Data handling
- External integrations
- Sensitive operations

---

### 2. Secrets & Sensitive Data Scan

Check for:
- Hardcoded credentials (API keys, passwords, tokens)
- Secrets inside:
  - Source code
  - Config files
  - Logs

If found:
- Report exact location (file + line if possible)
- Classify severity (HIGH)
- Describe exposure risk (e.g., unauthorized access, data breach)

---

### 3. Input Validation & Injection Analysis

Verify ALL external inputs:
- Request body
- Query params
- Headers
- File uploads

Check for:
- Missing validation
- Missing sanitization
- Direct usage in:
  - SQL queries
  - Commands
  - HTML rendering

Flag:
- SQL Injection
- XSS
- Command Injection

---

### 4. Authentication & Authorization

Validate:
- Is authentication required where needed?
- Is authorization enforced per resource?

Check for:
- Missing auth
- Broken role checks
- IDOR (Insecure Direct Object Reference)

---

### 5. Dependency & Supply Chain Audit

Inspect:
- New libraries added
- Existing dependencies

Check for:
- Outdated packages
- Known vulnerabilities
- Non-existent (hallucinated) dependencies

---

### 6. Dangerous APIs & Patterns

Search for:
- eval / dynamic execution
- Reflection misuse
- Unsafe deserialization
- Direct system calls

---

### 7. Cryptography & Data Protection

Verify:
- Weak algorithms (MD5, SHA1)
- Improper hashing
- Insecure token handling

Check:
- Exposure of sensitive data

---

### 8. Configuration & Security Controls

Validate:
- CORS not set to "*"
- Missing security headers
- No rate limiting
- Insecure defaults

---

### 9. Logging & Data Exposure

Check logs for:
- Tokens
- Passwords
- PII

Ensure:
- No sensitive data leakage
- No verbose error exposure

---

### 10. Business Logic & Runtime Risks

Analyze:
- Broken flows
- Privilege escalation paths
- Missing validation in logic

---

### 11. Risk Classification

Each finding MUST include:

- Severity:
  - HIGH
  - MEDIUM
  - LOW

- Impact
- Attack scenario

---

### 12. Output Format

Return ONLY findings.

DO NOT include fixes.

Format:

[SEVERITY] Issue Title  
Location: file:line  
Description:  
Impact:  
Attack Scenario:

---

## Rules

- This skill is STRICTLY READ-ONLY
- NEVER include code patches or fixes
- ALWAYS assume code may be insecure
- ALWAYS check OWASP patterns
- PRIORITIZE real exploitability
- DO NOT skip steps
- KEEP output objective and technical