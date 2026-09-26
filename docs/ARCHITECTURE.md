# Architecture

Status: groundwork. Only the modules below exist. See `docs/adr/0001-groundwork.md`,
`docs/adr/0002-ui-architecture.md`, `docs/adr/0003-module-boundaries.md` and
`docs/adr/0004-third-party-isolation.md` for why.

## Module types
Three types, no more — general rule, template for future projects too; full reasoning and the
rejected alternatives (a "capability" module namespace, a "shared" feature module, a networking
module) are in `docs/adr/0003-module-boundaries.md`.

| Type | Responsibility | May contain UI? |
|---|---|---|
| `app` | Wiring only: hosts a `feature/*/impl` screen, manual DI. No logic of its own. | Only what's needed to host a screen |
| `core/<concern>` | One capability, wrapped behind an interface we own (`Clock`; later `AlertPlayer`, `RoutineStore` — modelled only once something needs it, `docs/adr/0004-third-party-isolation.md`). `core/model` is the shared vocabulary every other module may reference. | No — except `core/designsystem`, the deliberate exception: the design system and the home of shared UI across features |
| `feature/<name>/api` | A feature's public contract. Pure Kotlin, no Android, no Compose, no `core/designsystem`. What another feature (or `app`) depends on to *reference* this feature. | No |
| `feature/<name>/impl` | The actual screen(s) for one feature. Built only from `core/designsystem` blocks; holds its own thin state layer over `core/model` (ADR 0002). | Yes |

## Modules (current instances)
| Module | Type | Contains |
|---|---|---|
| `core/model` | `core`, pure Kotlin JVM | `Clock`, `FakeClock`, traffic colour and shrink maths (SPEC §4, §6). Later: routine, phases, handover policies, quick timer, run-state reducer. |
| `core/designsystem` | `core` (UI exception) | Tokens, `KidsClockTheme`, `Kc*` blocks (see `docs/DESIGN_SYSTEM.md`). Later: pictograms, fonts. |
| `feature/run/api` | feature api, pure Kotlin JVM | `RunFeature.ID` — placeholder; grows a real contract when something needs to reference this feature. |
| `feature/run/impl` | feature impl, Android library (Compose) | Child run screen. |
| `app` | Android application | `MainActivity`, wiring (manual DI, a small `AppContainer` when needed). |
| `build-logic/convention` | included build | Convention plugins: `kidsclock.kotlin.jvm`, `.android.library`, `.android.application`, `.android.compose`, `.feature.api`, `.feature.impl`, `.android.snapshot`, `.android.accessibility`, `.android.uitest`, `.quality`, `.dependency.rules` (root only). Versions and SDK levels live here and in `gradle/libs.versions.toml`. |

Planned, not created yet: `core/data` (persistence, media store), `core/platform` (keep-awake, alerts, audio focus, lock task — the first one built the moment something needs it, ADR 0004), `feature/setup` (`api`/`impl`).

## Dependency rules
```
core/model            -> nothing
core/<other concern>   -> core/model only                         (never another core/<concern>)
core/designsystem       -> nothing
feature/<x>/api         -> core/model only                        (never Compose, designsystem, or another feature)
feature/<x>/impl        -> core/model, core/designsystem, any core/<concern> it needs,
                            its own api (automatic), any feature/<y>/api               (never feature/<y>/impl)
app                      -> anything
```
- No lateral `core`-to-`core` dependency; a type two `core/<concern>` modules both need belongs in `core/model`.
- No `feature`-to-`feature` dependency except `impl -> another feature's api`.
- `implementation`, not `api` (the Gradle configuration), by default everywhere — a module's own dependencies must not leak into its consumers' compile classpath.
- `core/model` has no `android.*` / `androidx.*` imports and no project dependencies, so it can move to Kotlin Multiplatform for iOS. It has its own `Clock`, and avoids JVM-only APIs where practical. Enforced by `:core:model:verifyModelPurity` (part of `check`).
- Material 3 is a private dependency of `core/designsystem`; `feature/*/impl` modules build UI from its `Kc*` blocks only (compile-enforced).
- **`verifyDependencyRules`** (root `check`, `kidsclock.dependency.rules`) mechanically checks every module's declared `implementation`/`api` dependencies against the table above, by Gradle-path pattern — a new `core/<concern>` or `feature/<name>` module is covered automatically.
- Compose only renders state. The state machine and all SPEC rules live in `core/model`, tested on the JVM.
- No `INTERNET` permission. Enforced by `:app:verifyNoInternetPermission` (merged manifest, debug and release).

## UI architecture
Unidirectional data flow (MVI-flavoured): `core/model`'s reducer decides everything; a `ViewModel`
per screen (manual DI) exposes `StateFlow<UiState>` for Compose to collect and a `Channel`/
`SharedFlow` for one-shot effects (never `StateFlow`, which redelivers its last value on
recomposition). `Kc*` blocks stay stateless: plain parameters and lambdas, no `ViewModel` or
`core/model` type passed in directly. Not yet built (`RunScreen` has no state yet); rationale and
rejected alternatives (classic MVVM, MVVM+C, VIPER) are in `docs/adr/0002-ui-architecture.md`.

## Timekeeping (design, not yet implemented)
- Time is derived from timestamps, never counted. `Clock.elapsedRealtimeMillis()` is implemented on Android over `SystemClock.elapsedRealtime()`.
- The run anchor is persisted as `{activityIndex, startedAtElapsed, bootId, pausedAccumulated}`. Elapsed = `now - start - paused`.
- If the boot id changed (reboot), elapsed-realtime is meaningless; fall back to a wall-clock timestamp saved with the anchor.
- A tick only means "recompute from timestamps". Effects (chime, persist, lock) are returned as data for the platform layer to run.
- To be validated on a real device in Phase 1 (screen-off, process kill, reboot).

## Tests
Policy: every piece of business logic is unit-tested in the same change (see `CLAUDE.md`, Testing rules).
- JUnit Jupiter + `kotlin.test` in every module. `core/model` gets them from `kidsclock.kotlin.jvm`; Android modules get the same stack from the Android convention plugins (`testDebugUnitTest` runs on the JVM, using `kotlin-test-junit5`).
- Test names follow SPEC sections, e.g. `Spec06_TrafficColour_isGreenAtStart`. ktlint naming rules are relaxed in `src/test` for this.
- Gates: `.githooks/pre-commit` and the Claude hook `.claude/hooks/check-changed.sh` run `scripts/check-changed.sh` (ktlint, guardrails, Android lint and unit/snapshot tests for changed modules). `.githooks/pre-push` and the Claude hook on `git push` run the full `./gradlew check`. Enable git hooks with `git config core.hooksPath .githooks`.
- Snapshot tests: Roborazzi + Robolectric (JVM, no emulator) for every UI component; baselines in `src/test/snapshots/`, verified by `check`, re-recorded with `./gradlew recordRoborazziDebug`. `verifySnapshotCoverage` fails a public `@Composable` file without `<File>SnapshotTest.kt`. Presentation only, no business logic.
- Test doubles: hand-written fakes, stubs and spies in `core/model` (KMP-friendly); MockK in Android modules only. Rules in `CLAUDE.md`.
- Isolation: JUnit class and method order is randomised for every module (convention plugins), so tests that depend on leftover state fail. Tests use `@TempDir` for files and restore any global state.
- Time is injected as `Clock`; tests use `FakeClock`.
- UI automation: every `Kc*` block requires a `testTag`; `KcScreen` turns on `testTagsAsResourceId`, so Maestro and UiAutomator can find elements by resource-id, on top of Compose UI tests via `onNodeWithTag`. `verifyAccessibleInteractions` (part of `check`) fails an interactive composable with no `testTag`, or an `Icon` with no `contentDescription`. Instrumented tests (`kidsclock.android.uitest`, on `app`/`feature/*/impl`) run with `./gradlew :app:connectedDebugAndroidTest`, not in the commit/push hooks (they need a device). Maestro flows live in `.maestro/`. Details and why: `docs/UI_AUTOMATION.md`.
- Not yet set up: Turbine, coverage reporting, CI. Add when needed.
