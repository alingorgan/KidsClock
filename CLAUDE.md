# KidsClock

A time display for young children (about 2–4½ years) and their caregivers. It shows **what we are doing now, how much of it is left, and what comes next**. It does not teach clock reading.

## Read first
1. `docs/SPEC.md` – the behaviour to build (platform-neutral).
2. `docs/DECISIONS.md` – why each choice was made, and how confident we are.
3. `docs/OPEN_QUESTIONS.md` – unresolved items. Do not guess these; ask.
4. `docs/RESEARCH.md` – the literature behind the design.

## The prototype is a behaviour reference, not code to port
`prototype/now-next-v2.html` is a single-file web proof of concept. Open it in a browser to see the behaviour. **Do not translate it line by line.** These parts are demo scaffolding and must not appear in the app:
- the speed menu (Real time / 20× / 60×), the "Jump near the end" and "Grown-up: unlock next" buttons, and the whole side panel
- the generated placeholder "SAMPLE PHOTO" / "SAMPLE CLIP" media
- the phone-shaped frame and bezel
- the demo start offset (the timer starting 20% in)
- tick-counting timers (see Engineering rules)

`prototype/now-next-v1.html` is the earlier, simpler version. `prototype/time-intel-brief.html` is the first research brief page.

## Stack and target
- Native Android, **Kotlin + Jetpack Compose**. iOS (Swift/SwiftUI) comes later, so keep behaviour in a platform-neutral core (see below) and follow `docs/SPEC.md` so both platforms match.
- Runs full screen on a phone, locked to the app (screen pinning / lock task). The child carries the phone between rooms.

## Engineering rules
- **Time is derived from timestamps, never counted.** Store the activity start time (monotonic clock) and compute elapsed and remaining time from it. The app must stay correct when the screen sleeps, the process is killed, or the device reboots.
- **Separate the state machine from the UI.** The routine, activity phases, handover policies and quick timers live in a plain Kotlin module with no Android imports, unit-tested. Compose only renders state. UI architecture (how Compose connects to it) is below and in ADR 0002.
- **Privacy: photos and clips never leave the device.** No network permission for media, no uploads, no analytics on media. Store in app-private storage. State this in the UI.
- **Colour is never the only signal.** Time left is always also shown as a shrinking shape, and the end is also signalled by sound.
- Keep the screen awake while running. Handle audio focus and silent mode deliberately.
- Small steps: plan first, then a thin vertical slice on a real device, then widen.

## Working style
- Ask before adding features not in the spec. Prefer the smallest change.
- Do not commit or push unless asked.

## Build, test, run
`adb`/`emulator` are not on PATH: use `~/Library/Android/sdk/platform-tools/adb` and `~/Library/Android/sdk/emulator/emulator`. See `.claude/skills/android-run/SKILL.md`.
```
./gradlew check                 # ktlint + Android lint (warnings are errors) + unit/snapshot tests + guardrails (run before finishing)
./gradlew :core:model:test      # fast pure-JVM tests
./gradlew ktlintFormat          # auto-format
./gradlew :app:installDebug     # install on the running emulator/device
```

## Testing rules
- **Every piece of business logic ships with unit tests in the same change.** Business logic means anything that decides behaviour: SPEC rules, state transitions, timing and progress maths, colour/scale mapping, handover policies, quick timers, persistence mapping, and any conditional or calculation an activity or phase depends on. Code without tests is not done.
- Put logic in `core/model` (pure Kotlin) so it is testable on the JVM. If logic lands in an Android module, extract it or test it in that module's `src/test`. Compose UI stays thin and is not a place to hide rules.
- Test the SPEC, not the implementation: name tests after the section, `Spec06_TrafficColour_isGreenAtStart`, and cover boundaries, clamping and every branch of a rule table.
- Time-dependent logic takes a `Clock`; tests use `FakeClock` and advance it. Never use real sleeps or `System.currentTimeMillis()`.
- **Isolate with test doubles.** A unit test exercises one unit; everything it collaborates with that is slow, non-deterministic or has side effects (time, storage, audio, media, permissions, Android APIs) is replaced by a double. Pick the lightest that works:
  - *Stub*: returns canned answers. *Fake*: small working implementation (`FakeClock`). *Spy*: records calls so the test asserts what happened (e.g. a `SpyAlertPlayer` collecting the chimes played). *Mock*: strict, verified interaction, for cases where a hand-written spy is overkill.
  - In `core/model` write doubles by hand against the interfaces (`Clock`, later `AlertPlayer`, stores): it must stay Kotlin-Multiplatform-ready, and MockK is JVM-only. Shared doubles live next to the interface, in `main` under a `testing` package.
  - In Android modules MockK is available (`mockk`, with `@ExtendWith(MockKExtension::class)`). Do not mock types you own that have a simple fake, and do not mock value types or the unit under test. Verify behaviour that matters, not every call.
  - Never touch the real clock, network, real files outside a temp dir, or real Android services from a unit test.
- **Leave no residual state.** Each test builds its own fixtures and doubles (no shared mutable fields, `object` singletons, statics or `@TestInstance(PER_CLASS)` state). Use JUnit `@TempDir` for any file. If a test changes global state (default locale/timezone, system properties, static mocks), restore it in `@AfterEach`. Test class and method order is randomised on every run to expose leaks; a test that fails only in some orders is a bug in the test. Re-run with the seed JUnit logs to reproduce.
- Fix bugs test-first: add a failing test that reproduces it, then fix.
- Stack: JUnit Jupiter + `kotlin.test`, configured for all modules by the convention plugins. Run `./gradlew check` before finishing; it must pass.
- **Automatic gates.** Before every commit, `scripts/check-changed.sh` runs for the changed files: ktlint, the module guardrails (`verifyModelPurity`, `verifyNoInternetPermission`, `verifySnapshotCoverage`, `verifyAccessibleInteractions`, `verifyDependencyRules`), Android lint (Android modules) and the unit + snapshot tests of the changed modules; build-file changes run everything. Before every push the full `./gradlew check` runs. Both are wired twice: git hooks in `.githooks/` (enable once per clone: `git config core.hooksPath .githooks`) and a Claude `PreToolUse` hook (`.claude/hooks/check-changed.sh`) that blocks Claude's `git commit`/`git push` and shows it the failure. Never bypass with `--no-verify`.
- **UI is snapshot-tested; logic is not.** Every UI component (each file with a public `@Composable`) has a `<File>SnapshotTest` covering its visual variants, using fixed inputs. Snapshot tests check presentation only, never business rules. Baselines live in `src/test/snapshots/`; `./gradlew check` verifies them; after an intended visual change run `./gradlew recordRoborazziDebug`, inspect the PNGs and commit them. See `docs/DESIGN_SYSTEM.md`.
- Wiring and build config need no unit tests, but say so in the summary if a change has none.

## UI architecture
- **Unidirectional data flow (MVI-flavoured), not MVVM+C or VIPER.** `core/model`'s reducer (`(state, event, now) -> state + effects`) is the only place decisions get made. A `ViewModel` per screen (manual DI, no Hilt) wraps it: exposes one `StateFlow<UiState>` that Compose collects, and one-shot effects (chime, navigation) via a `Channel`/`SharedFlow`, never via `StateFlow`. `Kc*` blocks take plain parameters and lambdas — never a `ViewModel` or a `core/model` type directly. Rationale and rejected alternatives: `docs/adr/0002-ui-architecture.md`.

## Design system
- UI is built from the "Lego blocks" in `core/designsystem` (`Kc*` composables and `KcTheme` tokens). Features never use Material 3 directly (it is not on their classpath) and never hard-code colours or dp values. Need a new look? Add a block plus its snapshot test. Details in `docs/DESIGN_SYSTEM.md`.

## UI automation
- **Every element is reachable by automation.** Every `Kc*` block takes a required `testTag` (id convention `<screen>.<element>`, e.g. `run.title`); icon-only controls also take a `contentDescription`. `KcScreen` turns on `testTagsAsResourceId`, so tags are queryable both from Compose UI tests (`onNodeWithTag`) and from UiAutomator-based tools (Maestro, `adb shell uiautomator dump`).
- `verifyAccessibleInteractions` (part of `check`) fails a build if an interactive composable has no `testTag`, or an `Icon` has no `contentDescription`. Opt out a genuinely decorative composable with `// kc-a11y-ignore: <reason>`.
- Instrumented (on-device) UI tests use Compose UI test / Espresso (`androidx.compose.ui.test.junit4`), set up by `kidsclock.android.uitest` on `app` and `feature/*/impl`. Run with `./gradlew :app:connectedDebugAndroidTest` against a running emulator/device; not part of the commit/push hooks, since they need a device.
- Maestro flows live in `.maestro/`; ids are the only selector, never translated text. Full details, including why, in `docs/UI_AUTOMATION.md`.

## Module rules
- **Three module types, no more: `app`, `core/<concern>`, `feature/<name>/{api,impl}`.** Every feature is two modules from day one, not promoted later. `api` is pure Kotlin (no Android, no Compose) and is the only thing another feature may depend on to reference this one; `impl` is the screen. Shared UI across features goes in `core/designsystem`; shared data types go in `core/model`. No "shared feature" module, no separate "capability" module namespace, no networking module (ever — see below). Full reasoning, meant as a reusable template: `docs/adr/0003-module-boundaries.md`. Scaffold a new one with `scripts/new-feature.sh <name>` — creates both modules, registers them, records snapshot baselines, verifies. It never wires the feature into `app`; that's a product decision.
- `core/model`: pure Kotlin. No `android.*`/`androidx.*` imports, no project dependencies (checked by `verifyModelPurity`).
- No lateral `core`-to-`core` dependency, and no `feature`-to-`feature` dependency except `impl -> another feature's api`. `core/designsystem` depends on nothing. `app` wires everything and may depend on anything. Mechanically enforced by `verifyDependencyRules` (part of `check`) — it reads every module's declared dependencies against these rules, so a new module is covered automatically.
- No `INTERNET` permission anywhere (checked by `verifyNoInternetPermission`).
- Tests are named after SPEC sections, e.g. `Spec06_TrafficColour_...`.
- Versions are pinned in `gradle/libs.versions.toml`; SDK levels in `build-logic/convention`.
- **Third-party and platform API isolation.** A library or Android platform API a capability depends on goes behind an interface we own, in the owning `core/<concern>` module (`Clock` over `SystemClock` is the model to follow) — model the interface only once something real needs it to vary (a second implementation, a test asserting on it, conditional behaviour) — a requirement merely being *named* in `CLAUDE.md`/SPEC is not that trigger. Exceptions, used directly, never wrapped: test-only frameworks, build-time Gradle plugins, and the UI framework substrate (Compose `ui`/`foundation`, Activity-Compose hosting) — Material 3 is the one substrate piece that *is* mirrored, via `core/designsystem`'s `Kc*` composables rather than a Kotlin `interface`. Full reasoning: `docs/adr/0004-third-party-isolation.md`.
