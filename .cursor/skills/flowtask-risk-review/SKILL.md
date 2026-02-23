---
name: flowtask-risk-review
description: Performs severity-first code review for FlowTask with focus on parser reliability, Android safety, and test adequacy. Use when the user asks for a review or before merge.
---

# FlowTask Risk Review

## Review Priorities

1. Correctness and regressions
2. Security and data exposure
3. Reliability and error handling
4. Test coverage and missing cases
5. Maintainability

## FlowTask-Specific Checks

- Parser behavior does not silently drop malformed AI output.
- Prompt injection/template changes preserve predictable behavior.
- Manifest/backup changes do not widen data exposure unintentionally.
- Debug or credential-adjacent files are not introduced to commits.
- Critical prompting behavior has unit tests for success and failure paths.

## Reporting Format

- **Critical:** must fix before merge
- **Important:** should fix in current branch
- **Minor:** optional improvements

Always include concrete file references and concise remediation guidance.
