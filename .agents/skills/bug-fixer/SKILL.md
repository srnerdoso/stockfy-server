---
name: bug-fixer
description:
  Use this skill to systematically identify, analyze, and fix bugs in code.
---

# Bug Fixer

This skill guides the agent in fixing bugs using a strict, reproducible workflow.

## Workflow

### 1. Identify Context

- Analyze the current workspace
- Determine affected files, scope, and system layer (frontend, backend, etc.)

---

### 2. Reproduce the Bug

- Identify exact steps to reproduce the issue
- Use logs, error messages, or user description
- If the bug cannot be reproduced:
    - STOP
    - Request more information

---

### 3. Isolate the Problem

- Narrow down to the smallest possible scope
- Identify the exact file, function, or logic involved
- Avoid analyzing unrelated code

---

### 4. Root Cause Analysis

- Identify the real cause (not the symptom)
- Trace the issue back to its origin using:
    - logs
    - stack traces
    - code inspection

> Fixing only symptoms leads to recurring bugs :contentReference[oaicite:0]{index=0}

---

### 5. Validate Hypothesis

- Form a clear hypothesis of the cause
- Confirm using minimal changes:
    - logs
    - breakpoints
    - small controlled tests

---

### 6. Apply Minimal Fix

- Fix ONLY the root cause
- Apply the smallest possible change
- **Do NOT** refactor unrelated code
- **Do NOT** optimize prematurely
- **Do NOT** introduce new abstractions

---

### 7. Validate the Fix

- Re-run the original scenario
- Ensure the bug is resolved
- Test related flows to detect side effects

> Bug fixes must always be retested to avoid introducing new issues

---

### 8. Final Review

- Confirm:
    - fix is correct
    - scope is minimal
    - no unrelated changes were introduced
    - The entire global scope of testing must pass successfully

---

## Rules

- Never fix a bug without reproducing it
- Never fix symptoms instead of root cause
- Never modify unrelated parts of the code
- Never downgrade or upgrade framework version
- Never downgrade or upgrade dependencies
- Always keep changes minimal and isolated
- Always validate before concluding

---

## Failure Handling

If any step cannot be completed:

- STOP execution
- Clearly state what is missing
- Request the required information

---

## Output Format

- Summary of the issue
- Root cause explanation
- Files and code sections affected
- Applied fix
- Validation steps performed