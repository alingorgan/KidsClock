# ADR 0001: Android groundwork

Status: accepted (2026-09-21). Follows `docs/INITIAL_SETUP_PLAN.md`.

## Decisions
- **minSdk 26** (owner-confirmed): old hand-me-down phones; modern audio and notification APIs. **compileSdk/targetSdk 36**: the latest stable platform installable via `sdkmanager` today. API 37 is not offered by the stable channel.
- **Toolchain**: Gradle 9.7.1, AGP 9.4.1, Kotlin 2.4.20 (AGP 9 built-in Kotlin, Compose compiler plugin), Compose BOM **2026.06.01**. Newer BOMs (2026.08.00 and 2026.09.00) require compileSdk 37, so the BOM is held back until 37 is stable. JDK 21 runs the build; bytecode target is 17.
- **Manual DI**, no Hilt: tiny app, avoids KSP and annotation-processing cost. Revisit if it grows.
- **Persistence deferred**: no `core/data` yet. DataStore/Room (or JSON in DataStore) and app-private media storage arrive with the first feature that needs them.
- **Kiosk deferred to a spike** (OPEN_QUESTIONS 13): screen pinning vs lock task vs device owner is decided after testing on a real device. Only keep-screen-on is done now.
- **Test stack**: JUnit Jupiter + `kotlin.test` in every module (Android modules via convention plugins, with `kotlin-test-junit5`). Policy: all business logic ships with unit tests, isolated with test doubles (hand-written in `core/model`, MockK 1.14.11 in Android modules), with randomised test order to catch residual state. Turbine, Compose UI tests and screenshot tests are added when there is something to test.
- **Module count**: four modules plus `build-logic`. `core/data`, `core/platform` and `feature/setup` are left out because nothing needs them yet; each extra module costs build time and boilerplate. Promote when a boundary is real.
- **`FakeClock` lives in `core/model` main**, not a test-fixtures source set, so it stays KMP-friendly and every module's tests can use it.
- **ktlint over detekt**: formatting-focused, near-zero config, few false positives for a tiny codebase. Add detekt later if complexity rules are wanted.
- **Guardrails** wired into `./gradlew check`: ktlint, Android lint (`abortOnError`, `MissingPermission` and `HardcodedText` as errors), unit tests, `verifyModelPurity`, `verifyNoInternetPermission`.
- **Existing AVD reused**: `Pixel_3a_API_32_arm64-v8a` already has an arm64 API 32 image, which satisfies minSdk 26, so no new system image was downloaded.
- **applicationId `com.kidsclock`** is a placeholder; rename before any release.

## Addendum: design system and snapshot tests
- **Design system as Lego blocks**: `core/designsystem` holds tokens, theme and `Kc*` blocks; Material 3 is private to it (compile-enforced for features). Rationale: consistent look, one place to change it, fewer decisions per screen, easy for agents to follow. See `docs/DESIGN_SYSTEM.md`.
- **Snapshot tests for every UI component**, presentation only. **Roborazzi 1.75.0 + Robolectric 4.17** chosen: mature, runs on the JVM (no emulator, fast in hooks and CI), baselines are plain PNGs in git, verified by `check`. Compose Preview Screenshot Testing was rejected as experimental; Paparazzi was not tried. Robolectric needs JUnit 4, so `junit-vintage-engine` runs beside Jupiter, and the test JVM needs `--add-exports java.base/jdk.internal.access` on JDK 21.
- **Coverage guardrail**: `verifySnapshotCoverage` (a filename convention check) fails if a public `@Composable` file lacks `<File>SnapshotTest.kt`. It checks presence, not quality of variants; review covers that.
- **Baselines are machine-rendered**: rendering can differ across OS or JDK versions. If CI on another platform shows diffs, record baselines there or add a tolerance.

## Addendum: linting and hooks
- **Android lint `warningsAsErrors = true`**: every warning fails the build so warnings never accumulate. `GradleDependency` and `NewerVersionAvailable` are disabled: versions are pinned on purpose (Compose BOM is held at 2026.06.01 until compileSdk 37 is stable) and those checks need the network, which breaks offline hooks.
- **Deliberate suppressions** live in the manifest with a reason: `LockedOrientationActivity` and `DiscouragedApi` for the portrait lock (phone-only; Android 16+ ignores fixed orientation on large screens, accepted).
- **Backup**: lint required `dataExtractionRules`/`fullBackupContent`. Both exclude everything from cloud backup and device transfer, in line with "photos and clips never leave the device". Revisit if users need to move a routine to a new phone (OPEN_QUESTIONS 6).
- **Hooks**: pre-commit (and the Claude commit hook) runs ktlint, guardrails, lint and tests for changed modules (about 10-30 s); pre-push (and the Claude push hook) runs full `check`. ktlint is check-only (no auto-format during a commit). Not added yet: detekt, compose-rules, CI (`--no-verify` is only backstopped by the pre-push gate and, later, CI).

## Addendum: UI automation
- **Required `testTag` on every `Kc*` block**, plus `contentDescription` on icon-only blocks: makes every element automatable and accessible by construction, instead of by discipline. A missing tag is a compile error (non-default parameter), backed by the `verifyAccessibleInteractions` heuristic guardrail for the raw `.clickable`/`Button`/`Icon` calls a block itself makes.
- **`testTagsAsResourceId` turned on in `KcScreen`**: the single place every screen passes through, so the flag is never forgotten per-screen. This is what lets UiAutomator-based tools (Maestro, `androidx.test.uiautomator`, `adb shell uiautomator dump`) find an element by its Compose `testTag`, not just Compose's own test API.
- **Maestro is not installed by tooling**: its official installer is `curl | bash`, which is not run automatically. `.maestro/smoke.yaml` and the install command are documented in `docs/UI_AUTOMATION.md`; the person running it installs it themselves.
- **Instrumented tests** (`kidsclock.android.uitest`: Compose UI test/Espresso, `androidx.test.uiautomator`, JUnit runner) verified end-to-end on the emulator: `MainActivitySmokeTest` finds `run.title` both via `onNodeWithTag` and via a raw UiAutomator `By.res(...)` lookup, proving the resource-id mapping Maestro depends on actually works, not just that Compose can see its own tree. Not wired into the commit/push hooks, since they need a running device; run by hand (`./gradlew :app:connectedDebugAndroidTest`) and add to CI later.
- **Id convention**: `<screen>.<element>`, stable and independent of visible text, since a tag must survive translation and content changes.
