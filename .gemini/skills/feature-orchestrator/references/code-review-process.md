# Reference: Code Review Process

## Objective

Ensure the analyzed code follows the project's architecture, respects defined conventions and standards, uses clear
naming, and maintains readability and maintainability.

## Core Principles

* **Source of Truth:** Use only rules present in the global context; do not invent rules.
* **Consistency:** Prioritize alignment with the existing project.
* **Objectivity:** Identify the specific problem and suggest a direct correction.
* **Focus:** Avoid generic comments, basic concept explanations, or praising the code.

---

## Review Workflow

### 1. Context Identification

Analyze the programming language, architectural layer (e.g., controller, service), and the specific responsibility of
the code snippet.

### 2. Architectural Validation

Verify the code is in the correct layer, respects separation of concerns, and avoids unnecessary coupling. Explain
errors and suggest reorganization if issues are found.

### 3. Conventions and Naming

* **Standards:** Compare file structure, imports, and formatting against project standards.
* **Naming:** Evaluate variables, functions, and classes for clarity, explicit intent, and consistency.

### 4. Code Quality Assessment

Identify overly large functions, code duplication, unnecessary complexity, or lack of proper abstraction.

---

## Output Standards

### Structured Review Format

Every identified issue must follow this structure:

* **Problem:** Direct description of the issue.
* **Impact:** Explanation of why it is problematic.
* **Suggestion:** Objective and actionable fix.

### Pull Request Generation (Read-Only)

Generate a professional description without modifying code or generating patches.

* **Scope:** Ensure changes represent a single concern (feature, fix, or refactor). Recommend splitting if multiple
  concerns are detected.
* **Title:** Technical and clear, reflecting the intent of the change.
* **Description Components:**
    * **Context:** Purpose of the change.
    * **Changes:** Summary of implementation.
    * **How to Test:** Validation steps.
    * **Risks:** Potential side effects.
* **Additional Info:** Mention related issues, highlight complex sections, and suggest a starting point for reviewers.