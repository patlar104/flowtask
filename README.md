# FlowTask

Android task app with a Compose-first UI and Conversational Flow foundation.

## Naming and identity baseline

- Product name: `FlowTask`
- Conversational surface name: `Conversational Flow`
- Android package + application ID: `com.patrick.flowtask`

## Current app foundation

- Multi-screen Compose shell (Home, Tasks, Conversational Flow, Settings)
- ViewModel-driven UI state with unidirectional event flow
- Persistent task repository backed by DataStore preferences
- Prompt injection and structured parser integration through a runtime conversation use-case
- Runtime AI client abstraction with:
  - fake provider in debug by default
  - HTTP provider path for production-style integration
  - typed client failure mapping

## Next phases

- Wire production endpoint contracts for your chosen AI provider
- Expand instrumentation coverage and offline behavior tests
- Add deeper release observability and performance benchmarks

## Runtime AI configuration

The HTTP AI path is backend-proxy oriented and reads:

- `AI_BACKEND_URL`

Do not ship long-lived provider API keys in the app. The mobile client should call your backend, and your backend should manage provider credentials or short-lived session tokens.

## Local tooling expectations

- Use a full JDK that includes `jlink` and `jmod` (JDK 17+ recommended for this project setup).
- Ensure your IDE Gradle JVM points to that full JDK, not a minimal runtime image.
