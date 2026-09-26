# Prompt 00: Android groundwork

Paste this into a fresh Claude Code session at the repo root.

---

You are setting up the groundwork for the KidsClock native Android app. Do groundwork only: no product features, no UI beyond an empty launchable screen.

## Read first (in order)
1. `CLAUDE.md`
2. `docs/INITIAL_SETUP_PLAN.md` (the approved plan: decisions, module structure, phases). Follow it. If you want to deviate, stop and ask.
3. `docs/SPEC.md`, `docs/DECISIONS.md`, `docs/OPEN_QUESTIONS.md`. Do not guess open questions.

Do not port `prototype/*`. Scope is now/next display only; no clock teaching.

## Fixed decisions
- Kotlin + Jetpack Compose, single Activity. minSdk 26. compileSdk/targetSdk = latest stable API level installed or installable (verify; expected 36).
- Gradle Kotlin DSL, version catalog `gradle/libs.versions.toml`, convention plugins in `build-logic/`, Gradle wrapper committed to the tree (do not commit to git; I will).
- Manual DI, no Hilt. No INTERNET permission anywhere.
- `core/model` is a pure Kotlin JVM module: no `android.*` imports, KMP-friendly (own `Clock` interface, no JVM-only APIs where avoidable).

## Environment facts
- macOS, Android Studio installed, JDK 21, SDK at `~/Library/Android/sdk` with only platform 32, build-tools 32 and AVD `Pixel_3a_API_32_arm64-v8a`. `adb`/`gradle` are not on PATH.
- Use `sdkmanager` (in `cmdline-tools`) to install the platform, build-tools and an arm64 system image. Tell me before any download over ~500 MB.

## Tasks
1. **Toolchain**: install the needed SDK packages; set `local.properties` (gitignored) and a `.gitignore` for Android/Gradle/IDE files. Report versions chosen and why.
2. **Scaffold** modules: `build-logic/convention`, `core/model` (JVM), `core/designsystem`, `feature/run`, `app`. Leave `core/data`, `core/platform`, `feature/setup` out until needed. Keep the module count minimal; note in the ADR why.
3. **Empty app**: `MainActivity` shows a placeholder Compose screen, keeps the screen on, edge-to-edge, portrait. Builds and launches on the AVD.
4. **`core/model` seed**: `Clock` interface, `FakeClock` for tests, and one pure function with tests as proof of the test loop: traffic colour by progress and shrink scale (SPEC §4 and §6). Name tests after spec sections, e.g. `Spec06_TrafficColour_...`. Nothing else.
5. **Guardrails**: `./gradlew check` runs ktlint or detekt (pick one, justify), Android lint (fail on errors), and unit tests. Add:
   - a test/check that the merged app manifest declares no `INTERNET` permission;
   - a check that `core/model` has no `android.*` imports and depends on no other project module.
6. **Docs**: `docs/ARCHITECTURE.md` (modules, dependency rules, timekeeping approach), `docs/adr/0001-groundwork.md` (minSdk, DI, persistence deferral, kiosk deferred to spike, test stack). Extend `CLAUDE.md` with build/test/run commands and the module rules. Keep it short.
7. **Agent config**: `.claude/settings.json` allowlisting `./gradlew`, `adb`, and emulator commands; add a project skill or short doc for build, install on the AVD, and screenshot.

## Constraints
- Smallest change that satisfies each task. Ask before adding anything not listed.
- Pin dependency versions in the catalog; prefer stable releases over alpha/beta.
- Do not commit or push.
- Do not touch `prototype/`.

## Done when (verify and show output)
- `./gradlew check` passes from a clean checkout.
- `./gradlew :core:model:test` passes and the tests cover the stop/blend points of the traffic colour.
- App installs and launches on the emulator (screenshot); `adb shell dumpsys package <id>` shows no INTERNET.
- Deliberately adding `import android.util.Log` to `core/model` makes the check fail (then revert it).
- Final report: what was created, versions chosen, anything skipped or uncertain, and a proposed prompt for the next phase (real-device spikes, plan Phase 1).
