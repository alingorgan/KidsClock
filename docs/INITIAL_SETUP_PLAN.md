# KidsClock Android: initial project setup

## Context
KidsClock v1 is a now / how-much-left / next display for ages ~2–4½ (not a clock-reading teacher; confirmed with owner). Docs (SPEC, DECISIONS, OPEN_QUESTIONS) and a web prototype exist; no code, no git commits yet. Goal of this plan: the groundwork (architecture, modules, SDK targets, tooling, AI-engineering setup) and a thin first slice, before widening. Nothing below is executed until approved.

Environment found: Android Studio installed, JDK 21, SDK has only platform 32 + build-tools 32 + one AVD (Pixel 3a API 32). No Gradle/adb on PATH. So: install a current platform (API 36) and use the Gradle wrapper.

## Decisions (recommended)
| Area | Choice | Why |
|---|---|---|
| minSdk | 26 (owner-confirmed) | Old hand-me-down phones; modern audio/notification APIs |
| target/compileSdk | 36 (latest stable at build time; verify) | Play requires recent target |
| Language/UI | Kotlin, Jetpack Compose, single Activity | Per CLAUDE.md |
| Build | Gradle Kotlin DSL, version catalog (`libs.versions.toml`), convention plugins in `build-logic/` | One place for versions/config; agents edit less boilerplate |
| DI | Manual DI (constructor injection, small `AppContainer`) | Tiny app; avoids Hilt/KSP overhead. Revisit if it grows |
| Persistence | DataStore (settings) + Room (routine) or JSON-in-DataStore for v1; media as files in app-private storage | Routine is small; start with the simplest that survives process death |
| Media | CameraX/`PickVisualMedia` (no storage permission), Media3 for clips, all app-private, no INTERNET permission | Privacy rule; a missing INTERNET permission is a build-time proof |
| Audio | SoundPool/AudioTrack synthesised bells behind an `AlertPlayer` interface, explicit audio focus (`AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`) | Spec §7; open Q15 |
| Timekeeping | `Clock` interface over `SystemClock.elapsedRealtime()`; persist start as elapsed-realtime + boot id + wall-clock fallback | "Time is derived, never counted"; survives kill/reboot (wall-clock fallback when boot id changes) |
| Kiosk | Spike first: screen pinning (`startLockTask`) vs device-owner lock task | Open Q13; do not commit to an approach yet |
| Testing | JUnit5/Kotest + Turbine for core; Compose UI tests + screenshot tests (Roborazzi/Paparazzi) for UI | Core is pure JVM = fast for agents |

## Module structure
```
KidsClock/
  build-logic/convention/          # android-app, android-library, kotlin-jvm-library plugins
  core/model/          (pure Kotlin JVM, NO android imports)
       Routine, Activity, Phase, HandoverPolicy, AgeMode, ProgressColourMode, QuickTimer
       RunState + reducer:  (state, event, now) -> state + effects
       TrafficColour / progress math (SPEC §5–6), shrink scale (§4)
  core/data/           (Android lib) DataStore/Room, media file store, persisted run anchor
  core/platform/       (Android lib) AlertPlayer, AudioFocus, KeepAwake, LockTask, ReduceMotion impls
  feature/run/         (Compose) child screen, orb, next tile, day strip, gate, sheet, media zoom
  feature/setup/       (Compose) age, activities, durations, policy, media, colour, sound
  core/designsystem/   (Compose) theme, tokens, pictograms, fonts
  app/                 (Android app) MainActivity, navigation, AppContainer wiring
  docs/                (existing) + docs/ARCHITECTURE.md, docs/adr/
```
Rules: `core/model` depends on nothing; `feature/*` depend on `core/model` + `core/designsystem` only; `app` wires implementations. Enforce with a Gradle dependency check (or Konsist/ArchUnit test). Split into modules only where a boundary is real: start with `core/model`, `app` (+ `feature/run`, `core/designsystem` as packages if module count is annoying) and promote later. iOS reuse: keep `core/model` as Kotlin Multiplatform-ready (no JVM-only APIs, `kotlinx-datetime`-free, own `Clock`); convert to KMP only when iOS starts. The state machine is specified by SPEC.md so a Swift port is testable against the same scenario tables (share fixtures as JSON in `docs/fixtures/`).

## State machine design (core/model)
- Single sealed `RunState`; events: `Tick(now)`, `ChildTap`, `GateOpened`, `CaregiverAction(...)`, `QuickTimer(...)`. `Tick` is only "recompute from timestamps"; nothing accumulates.
- Persisted anchor = `{activityIndex, startedAtElapsed, bootId, pausedAccumulated}`. On resume compute `elapsed = clock.now - start - paused`.
- Effects (play chime, request lock, persist) returned as data; platform layer executes. Makes every SPEC rule table-testable.
- Open questions (interrupted-activity resume, grace period, final-activity signal, ad-hoc dots) become configuration flags/default-off, logged in `docs/DECISIONS.md`; not guessed.

## AI-driven engineering setup
- Extend `CLAUDE.md` (keep short): build/test commands, module rules, "core/model has no Android imports", "no INTERNET permission", how to run on device.
- `docs/ARCHITECTURE.md` + `docs/adr/0001…` for the choices above (minSdk, DI, persistence, kiosk outcome).
- Spec-to-test traceability: name tests after SPEC sections (`Spec06_TrafficColour_…`); acceptance scenarios as fixtures.
- Guardrails agents can't skip: `./gradlew check` = ktlint/detekt + lint + unit tests; Android lint set to fail on `MissingPermission`/`HardcodedText`; a test asserting merged manifest has no `INTERNET`.
- Project `.claude/settings.json` allowlist for `./gradlew`, `adb`, emulator; a `/run`-style project skill that builds, installs on the AVD, screenshots.
- CI (GitHub Actions): `check` + unit tests + assemble on PR. Add later, not day 1.
- Workflow: thin vertical slice per PR; agents get one SPEC section per task; ask before spec-external features (CLAUDE.md).

## Phased path
0. **Groundwork**: git init commit of existing docs/prototype (only when you ask), install SDK 36 + API 34 image, create Gradle project + version catalog + convention plugins, empty app launches on emulator, CI-less `./gradlew check` green.
1. **Spikes on real device (before building on assumptions)**: (a) screen pinning vs lock task (Q13); (b) elapsed-realtime survives screen-off/kill/reboot; (c) audio focus + silent mode chime; (d) keep-screen-on battery/heat.
2. **Thin slice**: `core/model` timeline + traffic colour + phase transitions with unit tests → Run screen for one hardcoded routine (orb, shrinking bubble, whole-screen colour, chime at end, tap-to-advance policy). No setup UI yet.
3. **Widen**: grown-up gate + sheet, handover policies, quick timer, persistence, then setup UI, media, age modes, accessibility (TalkBack, reduce motion, shape+sound redundancy).
4. Child user testing on the open design questions (Q7–Q12) before hardening.

## Risks / things to watch
- Kiosk goal may need device-owner provisioning (`adb dpm set-device-owner`), acceptable for a single family device, not for distribution. Decide after spike.
- Doze/OEM battery killers can kill the process: correctness must come from timestamps, and the end chime needs an exact alarm or foreground service if the screen may sleep (verify need; the app keeps screen on while running).
- Play "Designed for Families"/child-directed policies apply if published; no ads/analytics SDKs keeps this simple.
- Compose animation of large gradients on low-end phones: verify frame rate on a real device early.

## Verification for this setup
- `./gradlew check` and `:core:model:test` pass on CLI.
- App installs and launches on the emulator; `adb shell dumpsys package` shows no INTERNET permission.
- Dependency-rule test fails if `core/model` imports `android.*`.
- Thin slice runs on the owner's real phone.

## First actions if approved (Phase 0 only)
Install SDK packages, scaffold Gradle project, write ARCHITECTURE.md + ADR-0001, extend CLAUDE.md. No commits made unless asked.
