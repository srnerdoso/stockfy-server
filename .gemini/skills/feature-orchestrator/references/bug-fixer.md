# Bug Fixer Reference Guide

**Description:** A rigorous, 8-step framework for identifying, isolating, and resolving software defects while
maintaining system integrity.

## Core Rules

* **Reproduction is Mandatory:** Never attempt a fix without first reproducing the bug.
* **Root Cause Only:** Eliminate the source of the error, not just the visible symptom.
* **Minimal Intervention:** Only modify code directly related to the fix.
* **No Scope Creep:** Do not refactor, optimize, or introduce new abstractions.
* **Environment Stability:** Never change framework versions or dependencies.
* **Strict Validation:** Every fix must be retested against the original scenario and global scope.

---

## The Workflow

| Step  | Phase                   | Key Actions                                                                              |
|:------|:------------------------|:-----------------------------------------------------------------------------------------|
| **1** | **Identify Context**    | Analyze workspace, scope, and system layers (Frontend/Backend).                          |
| **2** | **Reproduce**           | Define exact steps; use logs/errors. **Stop** if irreproducible.                         |
| **3** | **Create Test**         | **Develop a specific, failing test case** that captures the bug before any code changes. |
| **4** | **Isolate**             | Narrow down to the specific file, function, or logic block.                              |
| **5** | **Root Cause Analysis** | Trace back via stack traces/code inspection to find the origin.                          |
| **6** | **Validate Hypothesis** | Use minimal changes (logs/breakpoints) to confirm the theory.                            |
| **7** | **Apply Minimal Fix**   | Implement the smallest possible change to address the root cause.                        |
| **8** | **Validate Fix**        | Re-run the reproduction scenario and check for side effects.                             |
| **9** | **Final Review**        | Confirm minimal scope and ensure all global tests pass.                                  |

---

## Detailed Breakdown

### Root Cause Analysis

> "Fixing only symptoms leads to recurring bugs."

Always prioritize finding the **origin** of the data corruption or logic failure. If a variable is `null`, don't just
add a null check—find out *why* it wasn't initialized.

### Failure Handling

If any step in the workflow cannot be completed (e.g., missing logs, unable to reproduce, ambiguous requirements):

1. **STOP** execution immediately.
2. **State** clearly what information or access is missing.
3. **Request** the specific data needed to proceed.

---

## Required Output Format

When concluding a task, provide the following summary:

* **Summary of the Issue:** Brief overview of the reported behavior.
* **Root Cause Explanation:** Technical breakdown of why the bug occurred.
* **Affected Components:** List of files and code sections modified.
* **Applied Fix:** Description of the logic changes implemented.
* **Validation Results:** Evidence that the reproduction scenario now passes and no regressions were found.