---
name: flowtask-implementation-guard
description: Applies a balanced pre-change and verification workflow for FlowTask Android development. Use when implementing features, refactors, or bug fixes in this repository.
---

# FlowTask Implementation Guard

## Purpose

Use this skill to keep implementation work fast but disciplined in this project.

## Workflow

1. Identify affected files and expected behavior changes.
2. Check for nearby tests and add or update tests when behavior changes.
3. Implement minimal changes needed to satisfy the requirement.
4. Run semantic verification (contracts and negative paths), then run relevant commands and capture outcomes.
5. Report what changed, why, and any remaining risks.

## Verification Defaults

- Kotlin/app logic change: `./gradlew test`
- Android resource or manifest change: `./gradlew lint`
- Broad change: `./gradlew test lint`
- Release-readiness touchpoint: `./gradlew assembleRelease`

If commands are long-running, use a longer wait or background monitoring until an exit code is confirmed.

## Output Format

Return results in this structure:

- **Changes:** files touched and intent
- **Semantic checks:** contract and behavior checks performed
- **Verification:** commands run and pass/fail
- **Risks:** unresolved edge cases or follow-ups
