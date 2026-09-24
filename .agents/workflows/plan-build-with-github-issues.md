---
description: An end-to-end workflow to plan, build, test, and open a PR for a GitHub issue using a subagent-driven approach.
---

# Plan and Build with GitHub Issues Workflow

When the user asks to work on a GitHub issue using this workflow, follow these steps strictly:

## 1. Initialization and Context Gathering
- **Input:** Obtain the GitHub Issue ID or URL from the user.
- **Fetch Details:** Use the `github-mcp-server` (`issue_read` tool) to fetch the issue's title, description, and labels.
- **Fetch Comments:** Explicitly read ALL comments on the issue to gather any additional requirements, discussions, or changes to the original scope.
- **Check Documentation:** Search and read relevant Obsidian documentation (in `docs/requirements/`, `docs/specs/`, `docs/modeling/`) to understand the domain context and existing architectural models.
- **Check Rules:** Read the global `AGENTS.md` (and local module `AGENTS.md` if applicable) to remind yourself of the project's architectural, testing, and formatting standards.

## 2. Preparation and Planning
- **Branch Creation:** Create a local git branch following the standard format: `[issue-number]-[short-summary]`. Do NOT synchronize it with the remote repository yet.
- **Create Plan:** Invoke the `writing-plans` skill to generate a step-by-step implementation plan based on the issue details and project rules.
- **Save Plan:** Save the generated plan to the `docs/superpowers/plans/` directory.
- **Mandate TDD:** The plan MUST explicitly require Test-Driven Development (writing tests before implementation) for every task.

## 3. Delegation (Subagent Driven)
- **Invoke Skill:** Invoke the `subagent-driven-development` skill.
- **Dispatch Subagent:** Dispatch a subagent and hand over the issue context along with the plan you just created.
- **Instructions to Subagent:** Instruct the subagent to execute the plan step-by-step, starting with TDD, and to make atomic commits following the project's commit message conventions.
- **Wait:** Wait for the subagent to complete the implementation and report success.

## 4. Verification and Pull Request
- **Quality Gate:** Once the subagent finishes, run `./gradlew build` to perform a full verification cycle (unit tests, Checkstyle, SpotBugs). Do NOT bypass or silence failures.
- **Doc Updates:** Verify if any Obsidian documentation needs to be updated based on the new implementation and update them if necessary.
- **Push Branch:** Push your local branch to the remote GitHub repository.
- **Create PR:** Use the `github-mcp-server` (`create_pull_request` tool) to open a Pull Request targeting the `dev` branch.
- **PR Description:** The PR description MUST strictly follow the template defined in `AGENTS.md` (Resumo, Mudanças, Impacto, Validação, Checklist).