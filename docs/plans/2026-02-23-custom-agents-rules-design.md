# Custom Agents and Rules Setup Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add balanced, reusable project guidance by creating project-scoped rules and skills, plus mirrored personal skills for reuse across repositories.

**Architecture:** Add three concise `.cursor/rules/*.mdc` files for always-on safety/process guidance and Kotlin/Android-specific practices. Add two project skills under `.cursor/skills/` for implementation guardrails and risk-first code review, then mirror those skills to `~/.cursor/skills/` for personal reuse.

**Tech Stack:** Cursor rules (`.mdc`), Cursor skills (`SKILL.md`), Markdown process docs.

---

### Task 1: Create project rules

**Files:**
- Create: `.cursor/rules/flowtask-balanced-core.mdc`
- Create: `.cursor/rules/flowtask-android-kotlin-quality.mdc`
- Create: `.cursor/rules/flowtask-security-hygiene.mdc`
- Test: N/A (validate by frontmatter correctness and concise content)

**Step 1: Write failing test**

No runtime test. Validation is structural: ensure each file has valid frontmatter (`description`, `alwaysApply` and optional `globs`) and actionable guidance.

**Step 2: Run test to verify it fails**

Run: `ls .cursor/rules`
Expected before implementation: directory or files do not exist.

**Step 3: Write minimal implementation**

Create three concise rules:
- Core balanced process and verification behavior
- Kotlin/Android quality specifics (error handling, parser behavior, test expectations)
- Security hygiene (logs/secrets/backup handling)

**Step 4: Run test to verify it passes**

Run: `ls .cursor/rules && rg "alwaysApply|description|globs" .cursor/rules/*.mdc`
Expected: all rule files present with valid frontmatter keys.

**Step 5: Commit**

```bash
git add .cursor/rules/*.mdc
git commit -m "chore(cursor): add balanced project rules for flowtask"
```

### Task 2: Create project skills

**Files:**
- Create: `.cursor/skills/flowtask-implementation-guard/SKILL.md`
- Create: `.cursor/skills/flowtask-risk-review/SKILL.md`
- Test: N/A (validate structure + discoverability metadata)

**Step 1: Write failing test**

No runtime test. Validation criteria:
- `name` is lowercase-hyphenated
- `description` includes WHAT and WHEN trigger terms
- Workflow/checklist is clear and concise

**Step 2: Run test to verify it fails**

Run: `ls .cursor/skills`
Expected before implementation: missing skill directories.

**Step 3: Write minimal implementation**

Create two focused skills:
- `flowtask-implementation-guard`: pre-change checks, verification commands, reporting template
- `flowtask-risk-review`: severity-first review rubric with Android/prompting-specific checks

**Step 4: Run test to verify it passes**

Run: `ls .cursor/skills/*/SKILL.md`
Expected: both skill files present and readable.

**Step 5: Commit**

```bash
git add .cursor/skills/*/SKILL.md
git commit -m "chore(cursor): add project skills for implementation and review"
```

### Task 3: Mirror reusable skills to personal scope

**Files:**
- Create: `~/.cursor/skills/flowtask-implementation-guard/SKILL.md`
- Create: `~/.cursor/skills/flowtask-risk-review/SKILL.md`
- Test: N/A (validate files exist in personal scope)

**Step 1: Write failing test**

No runtime test. Validation is file presence and identical semantics to project skills.

**Step 2: Run test to verify it fails**

Run: `ls ~/.cursor/skills`
Expected before implementation: directory or specific skills missing.

**Step 3: Write minimal implementation**

Mirror the two project skills with reusable wording (still compatible with flowtask conventions).

**Step 4: Run test to verify it passes**

Run: `ls ~/.cursor/skills/flowtask-*/SKILL.md`
Expected: both personal skill files exist.

**Step 5: Commit**

```bash
# Personal-scope files are outside repo and won't be committed.
git status --short
```

### Task 4: Document outcomes and verification notes

**Files:**
- Modify: `docs/plans/2026-02-23-custom-agents-rules-design.md`
- Test: N/A

**Step 1: Write failing test**

No runtime test. Verification is that documentation references actual created files and explains intended usage.

**Step 2: Run test to verify it fails**

Run: `rg "flowtask-implementation-guard|flowtask-risk-review|flowtask-balanced-core" docs/plans/2026-02-23-custom-agents-rules-design.md`
Expected before update: missing final usage notes.

**Step 3: Write minimal implementation**

Append a short "Created Artifacts" section with exact paths and usage intent.

**Step 4: Run test to verify it passes**

Run: same `rg` command.
Expected: entries found.

**Step 5: Commit**

```bash
git add docs/plans/2026-02-23-custom-agents-rules-design.md
git commit -m "docs(plan): record custom cursor agents and rules setup"
```

---

## Created Artifacts

### Project rules

- `.cursor/rules/flowtask-balanced-core.mdc` - Always-on balanced execution defaults.
- `.cursor/rules/flowtask-android-kotlin-quality.mdc` - Kotlin/Android quality guidance for app source files.
- `.cursor/rules/flowtask-security-hygiene.mdc` - Security and data-hygiene safeguards.

### Project skills

- `.cursor/skills/flowtask-implementation-guard/SKILL.md` - Implementation checklist and verification defaults.
- `.cursor/skills/flowtask-risk-review/SKILL.md` - Severity-first review rubric.

### Personal mirrored skills

- `~/.cursor/skills/flowtask-implementation-guard/SKILL.md` - Reusable implementation guard.
- `~/.cursor/skills/flowtask-risk-review/SKILL.md` - Reusable risk-focused review skill.
