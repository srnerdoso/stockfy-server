---
name: feature-orchestrator
description:
  Coordinates the full lifecycle of a new feature, from conception and testing to security auditing,
  code review, and Pull Request creation.
---

# Feature Orchestrator

## Main Workflow

### 1. Define Expected Behavior

Before writing any code, analyze the requirements to determine inputs, outputs, business rules, and API contracts.

### 2. Test Creation (TDD)

Create integration and unit tests that serve as behavioral specifications before implementation.

* **Flow:** Define positive, negative, and edge scenarios without modifying production code.
* **Reference:** [testing-guidelines](./references/testing-guidelines.md).

### 3. Feature Implementation

Develop the minimal code required to satisfy the tests, following the project's architecture.

* **Flow:** Add new files as needed, respecting existing code integrity.
* **Reference:** [write-code-guidelines](./references/write-code-guidelines.md).

### 4. Test Execution and Validation

Run the test suite.

* **If tests fail:** Trigger the bug-fixing workflow to identify the root cause and apply minimal corrections.

    * **Reference:** [bug-fixer](./references/bug-fixer.md).

* **If tests pass:** Proceed to the Security step.

### 5. Security Audit

Conduct a strict scan for vulnerabilities (injection, data exposure, auth flaws).

* **Flow:** Identify risks without modifying code. If issues are found, return to the correction step and
  repeat tests.
* **Reference:** [security-audit-process](./references/security-audit-process.md).

### 6. Code Review

With functional and secure code, validate adherence to standards, naming conventions, and system architecture.

* **Flow:** Identify convention deviations and suggest readability improvements.
* If code does not pass review: Apply refactoring rules and improvement guidelines as defined in
  [refactoring-guidelines](./references/refactoring-guidelines.md).
* **Reference:**
  [code-review-process](./references/code-review-process.md), [refactoring-guidelines](./references/refactoring-guidelines.md).

### 7. Correction Cycle and Finalization (PR)

* **If code is rejected in review:** Fix the highlighted points. **Important:** After each correction, you must
  re-run security checks (Step 5) and code review (Step 6).
* **If accepted:** Generate a professional Pull Request description including context, changes, and risks.
* **Reference:** [code-review-process](./references/code-review-process.md).

---

## Golden Rules of the Skill

* **Integrity:** Never modify existing files or dependencies without explicit permission.
* **Test Priority:** No feature code should be written before behavior tests.
* **Quality Loop:** Any change made to fix a bug or meet a code review automatically restarts
  the security audit and review cycle.
* **Minimalism:** Keep fixes focused only on the root cause, avoiding unnecessary refactoring during the fix phase.

## Related Reference Files

| Skill               | Location                                                         |
|:--------------------|:-----------------------------------------------------------------|
| **Bug Fixer**       | [bug-fixer](./references/bug-fixer.md)                           |
| **Code Review**     | [code-review-process](./references/code-review-process.md)       |
| **Feature Builder** | [write-code-guidelines](./references/write-code-guidelines.md)   |
| **Refactor Code**   | [refactoring-guidelines](./references/refactoring-guidelines.md) |
| **Security Audit**  | [security-audit-process](./references/security-audit-process.md) |
| **Test Builder**    | [testing-guidelines](./references/testing-guidelines.md)         |
