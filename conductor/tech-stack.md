# Technology Stack: FlowTask

## Core Platform
- **Operating System:** Android (Min SDK: 28, Target/Compile SDK: 36)
- **Language:** Kotlin (2.2.10) - Following the official Kotlin Style Guide.
- **Latest Features:** Utilization of Android 14/15/16 (SDK 36) APIs, including enhanced privacy controls and UI optimizations.

## UI Framework
- **Primary Framework:** Jetpack Compose (Modern, declarative UI).
- **Design System:** Material 3 with Dynamic Color support.
- **Development Best Practices:** Following the latest Compose UI/UX patterns (Slot APIs, State Hoisting, and Unidirectional Data Flow).

## Architecture and Infrastructure
- **Dependency Injection:** Hilt (Recommended) for modular and testable code.
- **Networking:** Ktor or Retrofit (Optimized for AI API interactions).
- **Build System:** Gradle Kotlin DSL with Version Catalogs for clean, central dependency management.
- **Code Health:** Android Lint and Detekt for static analysis.

## AI and Conversational Logic
- **Integration:** Scalable architecture to support on-device (ML Kit) or cloud-based (Gemini/OpenAI) AI.
- **Pipeline:** Custom Prompt Engineering Pipeline with context injection, parameter configuration (`AiConfig`), and automated prompt batch-testing.
- **Output Parsing:** Robust structured response extraction using `kotlinx.serialization`.
- **Interaction:** Custom conversational Compose components designed for high responsiveness.

## Testing
- **Automated Testing:** Comprehensive Unit and UI test suites using JUnit 4 and Compose Test.
- **Quality Assurance:** Focus on >80% code coverage for core AI and task logic.
