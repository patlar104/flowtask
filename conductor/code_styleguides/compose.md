# Jetpack Compose Style Guide

## 1. Composable Functions
- **Naming:** Always use PascalCase for `@Composable` functions that return `Unit` (e.g., `HomeScreen`).
- **Modifiers:**
    - Every public Composable should accept a `modifier: Modifier = Modifier` as its first optional parameter.
    - Chain modifiers in a logical order (e.g., `padding` then `background`).

## 2. State Management
- **Hoisting:** Hoist state to the highest relevant caller to make components reusable and testable.
- **Remember:** Use `remember` and `rememberSaveable` to persist state across recompositions and configuration changes.
- **State Holders:** For complex logic, use `ViewModel` or dedicated State objects.

## 3. UI Patterns
- **Themes:** Use `MaterialTheme` for colors, typography, and shapes.
- **Previews:** Provide `@Preview` functions for different screen sizes and themes (Light/Dark mode).
- **Performance:** Avoid expensive calculations inside Composable functions; move them to `remember` blocks or the `ViewModel`.
