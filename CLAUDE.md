# Emu-X — notes for AI assistants

Android emulator frontend (Kotlin + Jetpack Compose, minSdk 26) wrapping libretro cores via LibretroDroid.

- Start with `graphify-out/GRAPH_REPORT.md` (code knowledge graph) and `docs/ARCHITECTURE.md` ("which file to edit" table).
- Refresh the graph after code changes: `./scripts/graphify.sh` (AST-only, no API key).
- Build: `./gradlew assembleDebug`; tests: `./gradlew testDebugUnitTest`; release: `./gradlew bundleRelease`.
- Cores (`app/src/main/jniLibs`) are not in git: run `./scripts/fetch-cores.sh` first.
- Code comments are in Thai on purpose (owner's request) — keep new comments in Thai.
- Every new system: `GameSystem.kt` + `ControllerLayouts.kt` + `CoreOptions.kt` + scripts.
- Core option keys in `CoreOptions.kt` were verified against each core's source; re-verify when bumping cores.
- Never bundle ROMs/BIOS or use console makers' logos (Play policy).
