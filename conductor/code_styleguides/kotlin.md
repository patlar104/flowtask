# Kotlin Style Guide

## 1. Naming Conventions
- **Classes/Objects:** PascalCase (e.g., `TaskRepository`).
- **Functions/Variables:** camelCase (e.g., `calculateFlow()`).
- **Constants:** UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`).

## 2. Formatting
- Use 4 spaces for indentation.
- Omit semicolons.
- Use expression bodies for simple one-line functions: `fun isReady() = status == Status.READY`.

## 3. Best Practices
- **Immutability:** Prefer `val` over `var` whenever possible.
- **Null Safety:** Use safe calls (`?.`) and Elvis operator (`?:`) instead of `!!`.
- **Scope Functions:** Use `let`, `apply`, `run`, and `also` appropriately to keep code concise.
- **Coroutines:** Always specify a `CoroutineDispatcher` (e.g., `Dispatchers.IO`) for background tasks.
